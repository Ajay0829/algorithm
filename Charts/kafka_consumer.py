"""
Kafka consumer for trading events
"""
import threading
import json
import logging
from kafka import KafkaConsumer
from config import KAFKA_BOOTSTRAP_SERVERS, KAFKA_TOPICS
from data_models import data_state

logger = logging.getLogger(__name__)

class TradingEventConsumer:
    """Manages Kafka consumption of trading events"""

    def __init__(self):
        self.consumer = None
        self.consumer_thread = None
        self.running = False

    def start_consumer(self):
        """Start the Kafka consumer in a background thread"""
        if not self.running:
            self.running = True
            self.consumer_thread = threading.Thread(target=self._consume_events, daemon=True)
            self.consumer_thread.start()
            logger.info("Kafka consumer started")

    def stop_consumer(self):
        """Stop the Kafka consumer"""
        self.running = False
        if self.consumer:
            self.consumer.close()
        logger.info("Kafka consumer stopped")

    def _consume_events(self):
        """Main consumer loop"""
        try:
            self.consumer = KafkaConsumer(
                *KAFKA_TOPICS,
                bootstrap_servers=KAFKA_BOOTSTRAP_SERVERS,
                value_deserializer=lambda m: json.loads(m.decode('utf-8')),
                auto_offset_reset='latest',
                enable_auto_commit=True
            )

            for msg in self.consumer:
                if not self.running:
                    break

                event = msg.value
                if not all(k in event for k in ('type', 'action', 'data')):
                    continue

                data = event['data']
                if data.get('timeframe') != '1d':
                    continue

                data_state.all_events_buffer.append(event)

        except Exception as e:
            logger.error(f"Error in Kafka consumer: {str(e)}")
        finally:
            if self.consumer:
                self.consumer.close()

# Global consumer instance
trading_consumer = TradingEventConsumer()
