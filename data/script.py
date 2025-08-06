#!/usr/bin/env python3
import os, time, csv, requests
from datetime import datetime
from zoneinfo import ZoneInfo
from zipfile import ZipFile
from urllib.parse import quote

# ─── CONFIG ────────────────────────────────────────────────────────────────────
API_TOKEN = "eyJ0eXAiOiJKV1QiLCJrZXlfaWQiOiJza192MS4wIiwiYWxnIjoiSFMyNTYifQ.eyJzdWIiOiIyUkFaSFMiLCJqdGkiOiI2ODhlZDgzNDkyMmMxNjBhNWQ5ZDI1ODEiLCJpc011bHRpQ2xpZW50IjpmYWxzZSwiaXNQbHVzUGxhbiI6ZmFsc2UsImlhdCI6MTc1NDE5MTkyNCwiaXNzIjoidWRhcGktZ2F0ZXdheS1zZXJ2aWNlIiwiZXhwIjoxNzU0MjU4NDAwfQ.d6_CeCKD0Zi1sTcfTFsJidETPZgxHGaoug_wAeco_VI"

instrument_map = {
     "NSE_EQ|INE585B01010": "MARUTI",
     "NSE_EQ|INE918I01026": "BAJAJFINSV",
     "NSE_EQ|INE089A01031": "DRREDDY",
     "NSE_EQ|INE917I01010": "BAJAJ-AUTO",
     "NSE_EQ|INE070A01015": "SHREECEM",
     "NSE_EQ|INE749A01030": "JINDALSTEL",
     "NSE_EQ|INE160A01022": "PNB",
     "NSE_EQ|INE646L01027": "INDIGO",
     "NSE_EQ|INE010B01027": "ZYDUSLIFE",
     "NSE_EQ|INE102D01028": "GODREJCP",
     "NSE_EQ|INE009A01021": "INFY",
     "NSE_EQ|INE376G01013": "BIOCON",
     "NSE_EQ|INE463A01038": "BERGEPAINT",
     "NSE_EQ|INE397D01024": "BHARTIARTL",
     "NSE_EQ|INE192R01011": "DMART",
     "NSE_EQ|INE237A01028": "KOTAKBANK",
     "NSE_EQ|INE059A01026": "CIPLA",
     "NSE_EQ|INE361B01024": "DIVISLAB",
     "NSE_EQ|INE797F01020": "JUBLFOOD",
     "NSE_EQ|INE030A01027": "HINDUNILVR",
     "NSE_EQ|INE795G01014": "HDFCLIFE",
     "NSE_EQ|INE028A01039": "BANKBARODA",
     "NSE_EQ|INE280A01028": "TITAN",
     "NSE_EQ|INE158A01026": "HEROMOTOCO",
     "NSE_EQ|INE123W01016": "SBILIFE",
     "NSE_EQ|INE192A01025": "TATACONSUM",
     "NSE_EQ|INE155A01022": "TATAMOTORS",
     "NSE_EQ|INE118A01012": "BAJAJHLDNG",
     "NSE_EQ|INE094A01015": "HINDPETRO",
     "NSE_EQ|INE528G01035": "YESBANK",
     "NSE_EQ|INE726G01019": "ICICIPRULI",
     "NSE_EQ|INE012A01025": "ACC",
     "NSE_EQ|INE095A01012": "INDUSINDBK",
     "NSE_EQ|INE669C01036": "TECHM",
     "NSE_EQ|INE216A01030": "BRITANNIA",
     "NSE_EQ|INE062A01020": "SBIN",
     "NSE_EQ|INE364U01010": "ADANIGREEN",
     "NSE_EQ|INE238A01034": "AXISBANK",
     "NSE_EQ|INE081A01020": "TATASTEEL",
     "NSE_EQ|INE044A01036": "SUNPHARMA",
     "NSE_EQ|INE075A01022": "WIPRO",
     "NSE_EQ|INE038A01020": "HINDALCO",
     "NSE_EQ|INE242A01010": "IOC",
     "NSE_EQ|INE205A01025": "VEDL",
     "NSE_EQ|INE685A01028": "TORNTPHARM",
     "NSE_EQ|INE121A01024": "CHOLAFIN",
     "NSE_EQ|INE860A01027": "HCLTECH",
     "NSE_EQ|INE179A01014": "PGHH",
     "NSE_EQ|INE742F01042": "ADANIPORTS",
     "NSE_EQ|INE047A01021": "GRASIM",
     "NSE_EQ|INE326A01037": "LUPIN",
     "NSE_EQ|INE584A01023": "NMDC",
     "NSE_EQ|INE414G01012": "MUTHOOTFIN",
     "NSE_EQ|INE018E01016": "SBICARD",
     "NSE_EQ|INE068V01023": "GLAND",
     "NSE_EQ|INE213A01029": "ONGC",
     "NSE_EQ|INE323A01026": "BOSCHLTD",
     "NSE_EQ|INE127D01025": "HDFCAMC",
     "NSE_EQ|INE021A01026": "ASIANPAINT",
     "NSE_EQ|INE733E01010": "NTPC",
     "NSE_EQ|INE176B01034": "HAVELLS",
     "NSE_EQ|INE545U01014": "BANDHANBNK",
     "NSE_EQ|INE239A01024": "NESTLEIND",
     "NSE_EQ|INE154A01025": "ITC",
     "NSE_EQ|INE406A01037": "AUROPHARMA",
     "NSE_EQ|INE101A01026": "M&M",
     "NSE_EQ|INE437A01024": "APOLLOHOSP",
     "NSE_EQ|INE090A01021": "ICICIBANK",
     "NSE_EQ|INE628A01036": "UPL",
     "NSE_EQ|INE196A01026": "MARICO",
     "NSE_EQ|INE018A01030": "LT",
     "NSE_EQ|INE121J01017": "INDUSTOWER",
     "NSE_EQ|INE140A01024": "PEL",
     "NSE_EQ|INE423A01024": "ADANIENT",
     "NSE_EQ|INE019A01038": "JSWSTEEL",
     "NSE_EQ|INE259A01022": "COLPAL",
     "NSE_EQ|INE522F01014": "COALINDIA",
     "NSE_EQ|INE296A01032": "BAJFINANCE",
     "NSE_EQ|INE765G01017": "ICICIGI",
     "NSE_EQ|INE002A01018": "RELIANCE",
     "NSE_EQ|INE203G01027": "IGL",
     "NSE_EQ|INE467B01029": "TCS",
     "NSE_EQ|INE079A01024": "AMBUJACEM",
     "NSE_EQ|INE129A01019": "GAIL",
     "NSE_EQ|INE481G01011": "ULTRACEMCO",
     "NSE_EQ|INE040A01034": "HDFCBANK",
     "NSE_EQ|INE114A01011": "SAIL",
     "NSE_EQ|INE603J01030": "PIIND",
     "NSE_EQ|INE003A01024": "SIEMENS",
     "NSE_EQ|INE663F01032": "NAUKRI",
     "NSE_EQ|INE066A01021": "EICHERMOT",
     "NSE_EQ|INE029A01011": "BPCL",
     "NSE_EQ|INE752E01010": "POWERGRID",
     "NSE_EQ|INE271C01023": "DLF",
     "NSE_EQ|INE318A01026": "PIDILITIND",
     "NSE_EQ|INE016A01026": "DABUR",
       "NSE_EQ|INE139A01034": "NATIONALUM",
       "NSE_EQ|INE758E01017": "JIOFIN",
       "NSE_EQ|INE848E01016": "NHPC",
       "NSE_EQ|INE267A01025": "HINDZINC",
       "NSE_EQ|INE982J01020": "PAYTM",
       "NSE_EQ|INE761H01022": "PAGEIND",
       "NSE_EQ|INE171Z01026": "BDL",
       "NSE_EQ|INE591G01025": "COFORGE",
       "NSE_EQ|INE494B01023": "TVSMOTOR",
       "NSE_EQ|INE814H01011": "ADANIPOWER",
       "NSE_EQ|INE0V6F01027": "HYUNDAI",
       "NSE_EQ|INE302A01020": "EXIDEIND",
       "NSE_EQ|INE134E01011": "PFC",
       "NSE_EQ|INE634S01028": "MANKIND",
       "NSE_EQ|INE619A01035": "PATANJALI",
       "NSE_EQ|INE465A01025": "BHARATFORG",
       "NSE_EQ|INE540L01014": "ALKEM",
       "NSE_EQ|INE377Y01014": "BAJAJHFL",
       "NSE_EQ|INE343G01021": "BHARTIHEXA",
       "NSE_EQ|INE811K01011": "PRESTIGE",
       "NSE_EQ|INE01EA01019": "VMM",
       "NSE_EQ|INE180A01020": "MFSL",
       "NSE_EQ|INE949L01017": "AUBANK",
       "NSE_EQ|INE881D01027": "OFSS",
       "NSE_EQ|INE476A01022": "CANBK",
       "NSE_EQ|INE721A01047": "SHRIRAMFIN",
       "NSE_EQ|INE670K01029": "LODHA",
       "NSE_EQ|INE298A01020": "CUMMINSIND",
       "NSE_EQ|INE674K01013": "ABCAPITAL",
       "NSE_EQ|INE274J01014": "OIL",
       "NSE_EQ|INE093I01010": "OBEROIRLTY",
       "NSE_EQ|INE073K01018": "SONACOMS",
       "NSE_EQ|INE006I01046": "ASTRAL",
       "NSE_EQ|INE562A01011": "INDIANB",
       "NSE_EQ|INE195A01028": "SUPREMEIND",
       "NSE_EQ|INE142M01025": "TATATECH",
       "NSE_EQ|INE849A01020": "TRENT",
       "NSE_EQ|INE111A01025": "CONCOR",
       "NSE_EQ|INE118H01025": "BSE",
       "NSE_EQ|INE883A01011": "MRF",
       "NSE_EQ|INE498L01015": "LTF",
       "NSE_EQ|INE338I01027": "MOTILALOFS",
       "NSE_EQ|INE935N01020": "DIXON",
       "NSE_EQ|INE002L01015": "SJVN",
       "NSE_EQ|INE377N01017": "WAAREEENER",
       "NSE_EQ|INE484J01027": "GODREJPROP",
       "NSE_EQ|INE031A01017": "HUDCO",
       "NSE_EQ|INE027H01010": "MAXHEALTH",
       "NSE_EQ|INE692A01016": "UNIONBANK",
       "NSE_EQ|INE04I401011": "KPITTECH",
       "NSE_EQ|INE263A01024": "BEL",
       "NSE_EQ|INE020B01018": "RECLTD",
       "NSE_EQ|INE647A01010": "SRF",
       "NSE_EQ|INE0BS701011": "PREMIERENE",
       "NSE_EQ|INE00H001014": "SWIGGY",
       "NSE_EQ|INE457A01014": "MAHABANK",
       "NSE_EQ|INE974X01010": "TIINDIA",
       "NSE_EQ|INE854D01024": "UNITDSPR",
       "NSE_EQ|INE226A01021": "VOLTAS",
       "NSE_EQ|INE171A01029": "FEDERALBNK",
       "NSE_EQ|INE262H01021": "PERSISTENT",
       "NSE_EQ|INE084A01016": "BANKINDIA",
       "NSE_EQ|INE775A01035": "MOTHERSON",
       "NSE_EQ|INE0LXG01040": "OLAELEC",
       "NSE_EQ|INE669E01016": "IDEA",
       "NSE_EQ|INE776C01039": "GMRAIRPORT",
       "NSE_EQ|INE211B01039": "PHOENIXLTD",
       "NSE_EQ|INE417T01026": "POLICYBZR",
       "NSE_EQ|INE813H01021": "TORNTPOWER",
       "NSE_EQ|INE415G01027": "RVNL",
       "NSE_EQ|INE335Y01020": "IRCTC",
       "NSE_EQ|INE931S01010": "ADANIENSOL",
       "NSE_EQ|INE821I01022": "IRB",
       "NSE_EQ|INE704P01025": "COCHINSHIP",
       "NSE_EQ|INE053F01010": "IRFC",
       "NSE_EQ|INE356A01018": "MPHASIS",
       "NSE_EQ|INE214T01019": "LTIM",
       "NSE_EQ|INE115A01026": "LICHSGFIN",
       "NSE_EQ|INE249Z01020": "MAZDOCK",
       "NSE_EQ|INE702C01027": "APLAPOLLO",
       "NSE_EQ|INE343H01029": "SOLARINDS",
       "NSE_EQ|INE388Y01029": "NYKAA",
       "NSE_EQ|INE117A01022": "ABB",
       "NSE_EQ|INE758T01015": "ETERNAL",
       "NSE_EQ|INE455K01017": "POLYCAB",
       "NSE_EQ|INE208A01029": "ASHOKLEY",
       "NSE_EQ|INE303R01014": "KALYANKJIL",
       "NSE_EQ|INE245A01021": "TATAPOWER",
       "NSE_EQ|INE053A01029": "INDHOTEL",
       "NSE_EQ|INE040H01021": "SUZLON",
       "NSE_EQ|INE399L01023": "ATGL",
       "NSE_EQ|INE092T01019": "IDFCFIRSTB",
       "NSE_EQ|INE347G01014": "PETRONET",
       "NSE_EQ|INE067A01029": "CGPOWER",
       "NSE_EQ|INE438A01022": "APOLLOTYRE",
       "NSE_EQ|INE121E01018": "JSWENERGY",
       "NSE_EQ|INE151A01013": "TATACOMM",
       "NSE_EQ|INE066F01020": "HAL",
       "NSE_EQ|INE257A01026": "BHEL",
       "NSE_EQ|INE774D01024": "M&MFIN",
       "NSE_EQ|INE0ONG01011": "NTPCGREEN",
       "NSE_EQ|INE647O01011": "ABFRL",
       "NSE_EQ|INE0J1Y01017": "LICI",
       "NSE_EQ|INE935A01035": "GLENMARK",
       "NSE_EQ|INE202E01016": "IREDA",
       "NSE_EQ|INE670A01012": "TATAELXSI",
       "NSE_EQ|INE200M01039": "VBL",
       "NSE_EQ|INE042A01014": "ESCORTS"
}

# These four windows exactly mirror your sample URLs:
#   • 2024-09-30 → 2024-12-01
#   • 2024-12-01 → 2025-03-01
#   • 2025-03-01 → 2025-06-01
#   • 2025-06-01 → 2025-09-01
quarters = [
    ("2024-12-01", "2024-09-30"),
    ("2025-03-01", "2024-12-01"),
    ("2025-06-01", "2025-03-01"),
    ("2025-09-01", "2025-06-01"),
]

tz = ZoneInfo("Asia/Kolkata")
out_dir = "nifty200_1h_csvs"
os.makedirs(out_dir, exist_ok=True)

BASE = "https://api.upstox.com/v3/historical-candle"
written = 0

for ikey, sym in instrument_map.items():
    print(f"\n➤ Processing {sym} ({ikey})")
    enc_key = quote(ikey, safe="_")  # only ‘|’ → ‘%7C’
    rows = []

    for idx, (to_dt, from_dt) in enumerate(quarters, start=1):
        url = f"{BASE}/{enc_key}/hours/1/{to_dt}/{from_dt}"
        print(f"  • Window {idx}/{len(quarters)} → {url}")
        resp = requests.get(url, headers={
            "Accept":        "application/json",
            "Authorization": f"Bearer {API_TOKEN}"
        })
#         print(resp.text)
        if resp.status_code != 200:
            print(f"    ⚠️ HTTP {resp.status_code}: {resp.text[:200]!r}")
            continue

        c = resp.json()['data']['candles']
        print(f"    → {len(c)} candles")
        for ts, o, h, l, c_, v, a in c:
            dt = datetime.fromisoformat(ts)
            rows.append((dt, o, h, l, c_, v))
        time.sleep(0.2)

    if not rows:
        print(f"  ✖️ No data for {sym}, skipping.")
        continue

    rows.sort(key=lambda x: x[0])
    path = os.path.join(out_dir, f"{sym}_1h.csv")
    with open(path, "w", newline="") as f:
        w = csv.writer(f)
        w.writerow(["datetime","open","high","low","close","volume"])
        for dt, o, h, l, c_, v in rows:
        # format as “2024-09-30 09:15:00+05:30” (no “T”)
            ts_str = dt.isoformat(sep=' ')
            w.writerow([ts_str, o, h, l, c_, v])

    print(f"  ✔️ Wrote {len(rows)} rows → {path}")
    written += 1

print(f"\n🔢 CSVs written: {written}")
print("📂 CSV folder:", os.listdir(out_dir))

zip_name = "nifty200_1h.zip"
with ZipFile(zip_name, "w") as zf:
    for fn in os.listdir(out_dir):
        zf.write(os.path.join(out_dir, fn), arcname=fn)
print(f"✅ Archive created: {zip_name}")
