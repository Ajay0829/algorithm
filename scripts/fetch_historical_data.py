# pip install kiteconnect pandas requests
import os, io, json, time, threading
from datetime import datetime, timedelta, timezone
from concurrent.futures import ThreadPoolExecutor, as_completed
import pandas as pd
from kiteconnect import KiteConnect

# ---------- CONFIG ----------
API_KEY = "XXX"
REQUEST_TOKEN = "XXX"
API_SECRET = "XXX"
MAX_WORKERS = 6   # safe for 3 rps limit
WINDOW_DAYS = 400 # Kite limit for hourly candles
SAVE_DIR = "saved"
os.makedirs(SAVE_DIR, exist_ok=True)
IST = timezone(timedelta(hours=5, minutes=30))
END_DATE = datetime.now(IST).replace(minute=0, second=0, microsecond=0)
START_DATE = END_DATE - timedelta(days=365*10 + 5)  # ~10 years

# ---------- INIT KITE ----------
# Step 1: Create KiteConnect instance with API key
kite = KiteConnect(api_key=API_KEY)

# Step 2: Exchange request_token for access_token
session_data = kite.generate_session(REQUEST_TOKEN, api_secret=API_SECRET)
access_token = session_data["access_token"]

# Step 3: Set the access token for all further API calls
kite.set_access_token(access_token)

print("Access Token:", access_token)

# ---------- LOAD SYMBOLS FROM JSON ----------
with open("instrument_key_map_NIFTY_100.json") as f:
    nifty100_map = json.load(f)
with open("instrument_key_map_NIFTY_NEXT_100.json") as f:
    niftynext100_map = json.load(f)

# Extract tradingsymbol values
symbols = sorted(set(list(nifty100_map.values()) + list(niftynext100_map.values())))
print(f"Total symbols loaded: {len(symbols)}")

# ---------- MAP SYMBOLS TO INSTRUMENT TOKENS ----------
inst_file = os.path.join(SAVE_DIR, "instruments_nse.csv")
if os.path.exists(inst_file):
    instruments = pd.read_csv(inst_file)
else:
    instruments = pd.DataFrame(kite.instruments("NSE"))
    instruments.to_csv(inst_file, index=False)

inst_map = (
    instruments
    .query("segment == 'NSE' and instrument_type == 'EQ'")
    .set_index("tradingsymbol")["instrument_token"]
    .to_dict()
)

missing = [s for s in symbols if s not in inst_map]
if missing:
    print("WARNING: Missing in instrument dump:", missing)
symbols = [s for s in symbols if s in inst_map]

# ---------- RATE LIMITER ----------
class RateLimiter:
    def __init__(self, max_per_sec=3):
        self.max_per_sec = max_per_sec
        self.ts = []
        self.lock = threading.Lock()
    def wait(self):
        with self.lock:
            now = time.time()
            self.ts = [t for t in self.ts if now - t < 1.0]
            if len(self.ts) >= self.max_per_sec:
                time.sleep(1.0 - (now - self.ts[0]))
            self.ts.append(time.time())

limiter = RateLimiter(max_per_sec=3)

# ---------- DATE WINDOWS ----------
def make_windows(start_dt, end_dt):
    cur = start_dt
    while cur < end_dt:
        nxt = min(cur + timedelta(days=WINDOW_DAYS), end_dt)
        yield cur, nxt
        cur = nxt

# ---------- FETCH LOGIC ----------
def file_path(sym): return os.path.join(SAVE_DIR, f"{sym}.csv")

def already_have(sym):
    p = file_path(sym)
    if not os.path.exists(p): return None
    df = pd.read_csv(p, parse_dates=["date"])
    if df.empty: return None
    last_ts = pd.to_datetime(df['date'].max(), utc=True).tz_convert(IST)
    return last_ts.to_pydatetime() + timedelta(hours=1)

def fetch_window(token, fdt, tdt):
    limiter.wait()
    return kite.historical_data(
        instrument_token=token,
        from_date=fdt.strftime("%Y-%m-%d %H:%M:%S"),
        to_date=tdt.strftime("%Y-%m-%d %H:%M:%S"),
        interval="60minute",
        continuous=False,
        oi=False
    )

def fetch_symbol(sym):
    token = inst_map[sym]
    start_dt = already_have(sym) or START_DATE
    if start_dt >= END_DATE:
        return sym, "up_to_date", 0

    frames = []
    rows = 0
    for fdt, tdt in make_windows(start_dt, END_DATE):
        try:
            data = fetch_window(token, fdt, tdt)
            if data:
                df = pd.DataFrame(data)
                df["date"] = pd.to_datetime(df["date"], utc=True).dt.tz_convert(IST)
                frames.append(df)
                rows += len(df)
        except Exception as e:
            print(f"[{sym}] {fdt}–{tdt} failed: {e}")

    if frames:
        out_df = pd.concat(frames, ignore_index=True).drop_duplicates("date").sort_values("date")
        p = file_path(sym)
        if os.path.exists(p):
            old = pd.read_csv(p, parse_dates=["date"])
            out_df = pd.concat([old, out_df], ignore_index=True).drop_duplicates("date").sort_values("date")
        out_df.to_csv(p, index=False)

    return sym, "ok", rows

# ---------- RUN ----------
results = []
with ThreadPoolExecutor(max_workers=MAX_WORKERS) as ex:
    futures = [ex.submit(fetch_symbol, s) for s in symbols]
    for fut in as_completed(futures):
        results.append(fut.result())

summary = pd.DataFrame(results, columns=["symbol", "status", "rows"])
summary.to_csv(os.path.join(SAVE_DIR, "summary.csv"), index=False)
print(summary["status"].value_counts())
