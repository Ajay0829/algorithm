#!/usr/bin/env python3
"""
Script to fetch historical candle data from Upstox API for all stocks in instrument_key_map.json
Generates 15m and 1h candles for 2024-2025 with rate limiting handling
"""

import json
import requests
import pandas as pd
import time
import os
from datetime import datetime, timedelta
from typing import Dict, List, Optional
import logging
from pathlib import Path
from urllib.parse import quote

# Set up logging
logging.basicConfig(
    level=logging.INFO,
    format='%(asctime)s - %(levelname)s - %(message)s',
    handlers=[
        logging.FileHandler('historical_data_fetch.log'),
        logging.StreamHandler()
    ]
)
logger = logging.getLogger(__name__)

class UpstoxHistoricalDataFetcher:
    """Fetches historical candle data from Upstox API with rate limiting"""

    def __init__(self, access_token: str):
        self.access_token = access_token
        # Use the correct API base URL - Upstox v3 API
        self.base_url = "https://api.upstox.com"
        self.headers = {
            "Authorization": f"Bearer {access_token}",
            "Accept": "application/json"
        }
        # Rate limiting: 250 requests per minute (approximately 4 requests per second)
        self.rate_limit_delay = 0.25  # 250ms between requests
        self.last_request_time = 0

        # Create output directory
        self.output_dir = Path("data/original")
        self.output_dir.mkdir(parents=True, exist_ok=True)

    def _wait_for_rate_limit(self):
        """Ensure we don't exceed rate limits"""
        current_time = time.time()
        time_since_last = current_time - self.last_request_time
        if time_since_last < self.rate_limit_delay:
            sleep_time = self.rate_limit_delay - time_since_last
            time.sleep(sleep_time)
        self.last_request_time = time.time()

    def _make_request(self, url: str, params: Dict) -> Optional[Dict]:
        """Make API request with rate limiting and error handling"""
        self._wait_for_rate_limit()

        try:
            response = requests.get(url, headers=self.headers, params=params, timeout=30)

            if response.status_code == 200:
                return response.json()
            elif response.status_code == 429:  # Rate limit exceeded
                logger.warning("Rate limit exceeded, waiting 60 seconds...")
                time.sleep(60)
                return self._make_request(url, params)  # Retry
            elif response.status_code == 401:
                logger.error("Unauthorized access. Check your access token.")
                return None
            else:
                logger.error(f"API request failed with status code: {response.status_code}")
                logger.error(f"Response: {response.text}")
                return None

        except requests.exceptions.RequestException as e:
            logger.error(f"Request failed: {e}")
            return None

    def fetch_historical_data(self, instrument_key: str, unit: str, interval: str,
                            from_date: str, to_date: str) -> Optional[pd.DataFrame]:
        """
        Fetch historical candle data for a given instrument

        Args:
            instrument_key: NSE instrument key (e.g., "NSE_EQ|INE585B01010")
            unit: Time unit ("minutes" or "hours")
            interval: Interval value ("15" for 15 minutes, "1" for 1 hour)
            from_date: Start date in YYYY-MM-DD format
            to_date: End date in YYYY-MM-DD format
        """
        # URL encode the instrument key to handle special characters like |
        encoded_instrument_key = quote(instrument_key, safe='')
        # Use the correct v3 API endpoint format: to_date comes before from_date in URL
        url = f"{self.base_url}/v3/historical-candle/{encoded_instrument_key}/{unit}/{interval}/{to_date}/{from_date}"

        logger.info(f"Fetching {interval}{unit} data for {instrument_key} from {from_date} to {to_date}")
        logger.info(f"Request URL: {url}")

        response_data = self._make_request(url, {})

        if not response_data or response_data.get("status") != "success":
            logger.error(f"Failed to fetch data for {instrument_key}")
            return None

        candles = response_data.get("data", {}).get("candles", [])

        if not candles:
            logger.warning(f"No candle data found for {instrument_key}")
            return None

        # Convert to DataFrame
        df = pd.DataFrame(candles, columns=["timestamp", "open", "high", "low", "close", "volume", "oi"])

        # Convert timestamp to datetime
        df["datetime"] = pd.to_datetime(df["timestamp"])
        df = df.drop("timestamp", axis=1)

        # Reorder columns
        df = df[["datetime", "open", "high", "low", "close", "volume", "oi"]]

        # Sort by datetime
        df = df.sort_values("datetime").reset_index(drop=True)

        return df

    def fetch_data_for_date_range(self, instrument_key: str, symbol: str,
                                unit: str, interval: str, start_date: str, end_date: str):
        """
        Fetch data for a date range, respecting Upstox API retrieval limits:
        - 15-minute intervals: 1 month max per request
        - 1-hour intervals: 1 quarter (3 months) max per request
        """
        start = datetime.strptime(start_date, "%Y-%m-%d")
        end = datetime.strptime(end_date, "%Y-%m-%d")

        all_data = []
        current_start = start

        # Determine chunk size based on interval
        if unit == "minutes" and int(interval) <= 15:
            # For intervals 1-15 minutes: Use 28 days to be safe (less than 1 month)
            chunk_days = 28
            logger.info(f"Using 28-day chunks for {interval}-minute interval")
        elif unit == "minutes" and int(interval) > 15:
            # For intervals >15 minutes: Use 85 days to be safe (less than 1 quarter)
            chunk_days = 85
            logger.info(f"Using 85-day chunks for {interval}-minute interval")
        elif unit == "hours":
            # For hour intervals: Use 85 days to be safe (less than 1 quarter)
            chunk_days = 85
            logger.info(f"Using 85-day chunks for {interval}-hour interval")
        else:
            # Default fallback
            chunk_days = 28
            logger.info(f"Using default 28-day chunks for {interval} {unit}")

        while current_start < end:
            # Calculate chunk end date based on API limits
            chunk_end = min(current_start + timedelta(days=chunk_days), end)

            chunk_start_str = current_start.strftime("%Y-%m-%d")
            chunk_end_str = chunk_end.strftime("%Y-%m-%d")

            # Skip if chunk_start equals chunk_end (no data to fetch)
            if chunk_start_str == chunk_end_str:
                current_start = chunk_end + timedelta(days=1)
                continue

            logger.info(f"Fetching chunk: {chunk_start_str} to {chunk_end_str}")

            chunk_data = self.fetch_historical_data(
                instrument_key, unit, interval, chunk_start_str, chunk_end_str
            )

            if chunk_data is not None and not chunk_data.empty:
                all_data.append(chunk_data)
                logger.info(f"Retrieved {len(chunk_data)} records for chunk")
            else:
                logger.warning(f"No data for chunk {chunk_start_str} to {chunk_end_str}")

            # Move to next chunk - use chunk_end as next start (no gap)
            current_start = chunk_end

            # Add delay between chunks to respect rate limits
            time.sleep(2)

        if not all_data:
            logger.warning(f"No data fetched for {symbol}")
            return None

        # Combine all chunks
        combined_df = pd.concat(all_data, ignore_index=True)
        combined_df = combined_df.drop_duplicates(subset=["datetime"]).sort_values("datetime")

        logger.info(f"Total records retrieved for {symbol}: {len(combined_df)}")
        return combined_df

    def save_to_csv(self, df: pd.DataFrame, symbol: str, interval_suffix: str):
        """Save DataFrame to CSV file"""
        filename = f"{symbol}_{interval_suffix}.csv"
        filepath = self.output_dir / filename

        df.to_csv(filepath, index=False)
        logger.info(f"Saved {len(df)} records to {filepath}")

    def has_existing_data(self, symbol: str) -> bool:
        """Check if both 15m and 1h data files already exist for a stock"""
        file_15m = self.output_dir / f"{symbol}_15m.csv"
        file_1h = self.output_dir / f"{symbol}_1h.csv"

        exists_15m = file_15m.exists()
        exists_1h = file_1h.exists()

        if exists_15m and exists_1h:
            logger.info(f"Skipping {symbol} - both 15m and 1h data files already exist")
            return True
        elif exists_15m:
            logger.info(f"{symbol} - 15m data exists, will fetch 1h data only")
            return False
        elif exists_1h:
            logger.info(f"{symbol} - 1h data exists, will fetch 15m data only")
            return False
        else:
            logger.info(f"{symbol} - no existing data found, will fetch both intervals")
            return False

    def get_missing_intervals(self, symbol: str) -> list:
        """Get list of intervals that need to be fetched for a stock"""
        file_15m = self.output_dir / f"{symbol}_15m.csv"
        file_1h = self.output_dir / f"{symbol}_1h.csv"

        intervals = []

        if not file_15m.exists():
            intervals.append(("minutes", "15", "15m"))

        if not file_1h.exists():
            intervals.append(("hours", "1", "1h"))

        return intervals

    def fetch_all_stocks(self, instrument_map_path: str):
        """Fetch data for all stocks in the instrument map"""
        # Load instrument mapping
        with open(instrument_map_path, 'r') as f:
            instrument_map = json.load(f)

        total_stocks = len(instrument_map)
        logger.info(f"Starting to fetch data for {total_stocks} stocks")

        # Check existing data files
        stocks_with_complete_data = 0
        stocks_to_process = 0

        # Date ranges for 2024-2025
        start_date = "2023-01-01"
        end_date = "2025-07-01"

        for i, (instrument_key, symbol) in enumerate(instrument_map.items(), 1):
            logger.info(f"Processing {symbol} ({i}/{total_stocks})")

            # Check if we need to fetch any data for this stock
            if self.has_existing_data(symbol):
                stocks_with_complete_data += 1
                continue

            stocks_to_process += 1

            # Get only the intervals that are missing
            intervals_to_fetch = self.get_missing_intervals(symbol)

            if not intervals_to_fetch:
                continue

            for unit, interval, suffix in intervals_to_fetch:
                try:
                    logger.info(f"Fetching {suffix} data for {symbol}")
                    df = self.fetch_data_for_date_range(
                        instrument_key, symbol, unit, interval, start_date, end_date
                    )

                    if df is not None and not df.empty:
                        self.save_to_csv(df, symbol, suffix)
                    else:
                        logger.warning(f"No data available for {symbol} - {interval}{unit}")

                except Exception as e:
                    logger.error(f"Error processing {symbol} - {interval}{unit}: {e}")
                    continue

            # Add delay between stocks to be respectful to the API
            time.sleep(2)

        logger.info(f"Completed fetching historical data:")
        logger.info(f"  - Stocks with complete data (skipped): {stocks_with_complete_data}")
        logger.info(f"  - Stocks processed: {stocks_to_process}")
        logger.info(f"  - Total stocks: {total_stocks}")

def main():
    """Main function to run the historical data fetcher"""

    # You need to provide your Upstox access token here
    access_token = input("Enter your Upstox access token: ").strip()

    if not access_token:
        logger.error("Access token is required")
        return

    # Path to instrument mapping file
    instrument_map_path = "instrument_key_map_NIFTY_NEXT_100.json"

    if not os.path.exists(instrument_map_path):
        logger.error(f"Instrument mapping file not found: {instrument_map_path}")
        return

    # Initialize fetcher and start processing
    fetcher = UpstoxHistoricalDataFetcher(access_token)
    fetcher.fetch_all_stocks(instrument_map_path)

if __name__ == "__main__":
    main()
