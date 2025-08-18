# pip install kiteconnect pandas python-dateutil
from kiteconnect import KiteConnect
import pandas as pd
from datetime import datetime, timedelta
from dateutil.relativedelta import relativedelta
import time

API_KEY = "p7zekgkiwxciimkq"
API_SECRET = "rom33v0c333akmo7pku2ph0yeck9b9fd"
REQUEST_TOKEN = "W3XAYcwJry3eWEbU1qXlfteu9PtkfZ0e"

# --------- CONFIG ----------
# Use absolute dates if you prefer, e.g. "2015-01-01" to "2025-08-15"
FROM_DATE_STR = None  # e.g. "2015-01-01"
TO_DATE_STR   = None  # e.g. "2025-08-15"
INTERVAL = "day"      # 1D timeframe
OUT_FILENAMES = {
    "NIFTY 50":  "NIFTY50_1D.csv",
    "NIFTY 100": "NIFTY100_1D.csv",
    "NIFTY 200": "NIFTY200_1D.csv",
}
# ---------------------------

# --- Auth ---
kite = KiteConnect(api_key=API_KEY)
session = kite.generate_session(REQUEST_TOKEN, api_secret=API_SECRET)
kite.set_access_token(session["access_token"])

# --- Date range ---
to_dt = datetime.today() if TO_DATE_STR is None else datetime.fromisoformat(TO_DATE_STR)
from_dt = to_dt - relativedelta(years=11) if FROM_DATE_STR is None else datetime.fromisoformat(FROM_DATE_STR)

# --- Resolve instrument tokens for indices from NSE instruments dump ---
def get_index_token(k, display_name, exchange="NSE"):
    instruments = k.instruments(exchange)
    df = pd.DataFrame(instruments)
    for col in ("tradingsymbol", "name", "segment", "instrument_type"):
        if col not in df.columns:
            df[col] = ""
    df["ts_lower"] = df["tradingsymbol"].astype(str).str.lower()
    df["name_lower"] = df["name"].astype(str).str.lower()

    # Keep only indices rows (naming can vary slightly)
    candidates = df[
        df["instrument_type"].astype(str).str.upper().eq("INDICES")
        | df["segment"].astype(str).str.upper().str.contains("INDICES", na=False)
    ].copy()

    aliases_map = {
        "NIFTY 50":  ["NIFTY 50", "NIFTY50", "NIFTY-50", "NIFTY_50"],
        "NIFTY 100": ["NIFTY 100", "NIFTY100", "NIFTY-100", "NIFTY_100"],
        "NIFTY 200": ["NIFTY 200", "NIFTY200", "NIFTY-200", "NIFTY_200"],
    }
    aliases = [a.lower() for a in aliases_map[display_name]]

    row = candidates[candidates["ts_lower"].isin(aliases)]
    if row.empty:
        row = candidates[candidates["name_lower"].isin(aliases)]
    if row.empty:
        mask = False
        for a in aliases:
            mask = mask | candidates["ts_lower"].str.contains(a, na=False) | candidates["name_lower"].str.contains(a, na=False)
        row = candidates[mask]

    if row.empty:
        preview = candidates[["tradingsymbol", "name", "instrument_token"]].head(15)
        raise RuntimeError(f"Could not find index '{display_name}' in NSE instruments.\nPreview:\n{preview}")

    row = row.iloc[0]
    return int(row["instrument_token"]), str(row["tradingsymbol"])

# --- Chunked historical fetch (keeps data RAW) ---
def fetch_historical_all(k, token, start_dt, end_dt, interval="day", sleep_sec=0.35):
    out = []
    cur_start = start_dt
    while cur_start < end_dt:
        cur_end = min(cur_start + relativedelta(days=365), end_dt)
        data = k.historical_data(
            instrument_token=token,
            from_date=cur_start,
            to_date=cur_end,
            interval=interval,
            continuous=False,
            oi=False
        )
        if data:
            out.extend(data)   # keep raw dicts
        cur_start = cur_end + timedelta(days=1)
        time.sleep(sleep_sec)  # rate-limit friendly
    return out

indices = ["NIFTY 50", "NIFTY 100", "NIFTY 200"]
resolved = {}
for name in indices:
    token, ts = get_index_token(kite, name, exchange="NSE")
    resolved[name] = {"token": token, "tradingsymbol": ts}

print("Resolved tokens:", resolved)

for name in indices:
    token = resolved[name]["token"]
    print(f"Fetching {name} ({token}) {INTERVAL} from {from_dt.date()} to {to_dt.date()} ...")
    raw = fetch_historical_all(kite, token, from_dt, to_dt, interval=INTERVAL, sleep_sec=0.35)
    df = pd.DataFrame(raw)  # RAW as returned by Kite; no renames, no extra cols, no formatting
    out_path = OUT_FILENAMES[name]
    df.to_csv(out_path, index=False)
    print(f"Saved {out_path}  rows={len(df)}")

print("Done.")
