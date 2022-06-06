# TMI Receiver

Reads Twitch Chat and sends messages to clients via redis pub/sub.

## Usage

Clients can request channels by sending SUBSCRIBE tmiReceiver {channel}
The messages then get published to tmiReceiver.{channel}
