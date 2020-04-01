//import * as Stomp from '@stomp/stompjs';
//import * as Stomp from '@stomp/stompjs';
import * as Stomp from 'stompjs';
import * as SockJS from 'sockjs-client';
import { Injectable } from '@angular/core';

@Injectable({
    providedIn: 'root',
})
export class WebSocketService {
    webSocketEndPoint: string = '/websocket';
    topic: string = '/topic/greetings';
    stompClient: any;
    constructor() {}
    _connect() {
        console.log('Initialize WebSocket Connection');
        let ws = new SockJS(this.webSocketEndPoint);
        this.stompClient = Stomp.over(ws);
        const _this = this;
        _this.stompClient.connect(
            {},
            function(frame) {
                _this.stompClient.subscribe(_this.topic, function(sdkEvent) {
                    _this.onMessageReceived(sdkEvent);
                });
                //_this.stompClient.reconnect_delay = 2000;
            },
            this.errorCallBack,
        );
    }

    _disconnect() {
        if (this.stompClient !== null) {
            this.stompClient.disconnect();
        }
        console.log('Disconnected');
    }

    // on error, schedule a reconnection attempt
    errorCallBack(error) {
        console.log('errorCallBack -> ' + error);
        setTimeout(() => {
            this._connect();
        }, 5000);
    }

    /**
     * Send message to sever via web socket
     * @param {*} message
     */
    _send(endPoint: string, message: string) {
        this.stompClient.send(`${endPoint}`, {}, message);
    }

    onMessageReceived(message) {
        console.log('Message Recieved from Server :: ' + message);
    }
}
