FROM python:3.9-slim

ARG topic
ARG bootstrap_servers 
ARG num_devices
ARG num_messages
ARG pause_ms

ENV TOPIC $topic
ENV BOOTSTRAP_SERVERS $bootstrap_servers
ENV NUM_DEVICES $num_devices
ENV NUM_MESSAGES $num_messages
ENV PAUSE_MS $pause_ms

WORKDIR /app

COPY . /app

RUN pip install --no-cache-dir -r requirements.txt

ENTRYPOINT python Producer.py --topic=${TOPIC} --bootstrap_servers=${BOOTSTRAP_SERVERS} --num_devices=${NUM_DEVICES} --num_messages=${NUM_MESSAGES} --pause_ms=${PAUSE_MS}

