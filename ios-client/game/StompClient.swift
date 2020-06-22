//
//  StompClient.swift
//  game
//
//  Created by Zhaolong Zhong on 6/21/20.
//  Copyright © 2020 Zhaolong Zhong. All rights reserved.
//

// Reference: https://github.com/WrathChaos/StompClientLib

import Foundation
import Starscream

struct StompCommands {
    // Basic Commands
    static let commandConnect = "CONNECT"
    static let commandSend = "SEND"
    static let commandSubscribe = "SUBSCRIBE"
    static let commandUnsubscribe = "UNSUBSCRIBE"
    static let commandBegin = "BEGIN"
    static let commandCommit = "COMMIT"
    static let commandAbort = "ABORT"
    static let commandAck = "ACK"
    static let commandDisconnect = "DISCONNECT"
    static let commandPing = "\n"
    
    static let controlChar = String(format: "%C", arguments: [0x00])
    
    // Ack Mode
    static let ackClientIndividual = "client-individual"
    static let ackClient = "client"
    static let ackAuto = "auto"
    // Header Commands
    static let commandHeaderReceipt = "receipt"
    static let commandHeaderDestination = "destination"
    static let commandHeaderDestinationId = "id"
    static let commandHeaderContentLength = "content-length"
    static let commandHeaderContentType = "content-type"
    static let commandHeaderAck = "ack"
    static let commandHeaderTransaction = "transaction"
    static let commandHeaderMessageId = "id"
    static let commandHeaderSubscription = "subscription"
    static let commandHeaderDisconnected = "disconnected"
    static let commandHeaderHeartBeat = "heart-beat"
    static let commandHeaderAcceptVersion = "accept-version"
    // Header Response Keys
    static let responseHeaderSession = "session"
    static let responseHeaderReceiptId = "receipt-id"
    static let responseHeaderErrorMessage = "message"
    // Frame Response Keys
    static let responseFrameConnected = "CONNECTED"
    static let responseFrameMessage = "MESSAGE"
    static let responseFrameReceipt = "RECEIPT"
    static let responseFrameError = "ERROR"
}

public enum StompAckMode {
    case AutoMode
    case ClientMode
    case ClientIndividualMode
}

public enum ConnectionState {
	case Connected
	case Connecting
	case Disconnected
}

class StompClient {
	private var socket: WebSocket?
	private var isConnected = false
	private var connectionState: ConnectionState = .Disconnected
	private var pingTimer: Timer? {
		willSet { pingTimer?.invalidate() }
	}
	
	private var connectionHeaders: [String: String] = [String: String]()
	
	init() {
		schedulePingTimer()
	}
	
	deinit {
		pingTimer = nil // cancel ping timer
	}
	
	private func schedulePingTimer() {
		let waitTime: TimeInterval = 5
		print("schedulePingTimer waitTime: 5s")
		DispatchQueue.main.async {
			self.pingTimer = Timer.scheduledTimer(timeInterval: waitTime, target: self, selector: #selector(self.pingTimerFired), userInfo: nil, repeats: true)
		}
	}
	
	@objc func pingTimerFired() {
		guard connectionState == .Connected else {
			print("Skip, due to connectionState: \(connectionState)")
			return
		}
		print("Send ping")
		socket?.write(ping: Data()) //example on how to write a ping control frame over the socket!
	}
	
	func testWebsocket() {
		print("testWebsocket")
		
		openConnection()
		
		if (socket != nil) {
			sendGreeting()
			sendChat()
		} else {
			print("socket is nil")
		}
	}
	
	func subscribeWebSocket() {
		subscribe(destination: "/topic/greeting")
		subscribe(destination: "/topic/chat")
	}
	
	func sendChat() {
		print("SendChat")
		let chatTopic = "/topic/chat"
		sendJSONForDict(dict: ["userId":"id_123", "message": "hello, i'm id_123"] as AnyObject, toDestination: chatTopic)
	}
	
	func sendGreeting() {
		print("SendGreeting")
		
		let greetingTopic = "/topic/greeting"
		var headerToSend = [String: String]()
		headerToSend[StompCommands.commandHeaderDestination] = greetingTopic
		sendFrame(command: StompCommands.commandSend, header: headerToSend, body: "hello world" as AnyObject)
	}
	
    public func sendJSONForDict(dict: AnyObject, toDestination destination: String) {
        do {
            let theJSONData = try JSONSerialization.data(withJSONObject: dict, options: JSONSerialization.WritingOptions())
            let theJSONText = String(data: theJSONData, encoding: String.Encoding.utf8)
			var headerToSend = [String: String]()
			headerToSend[StompCommands.commandHeaderDestination] = destination
			sendFrame(command: StompCommands.commandSend, header: headerToSend, body: theJSONText as AnyObject)
        } catch {
            print("error serializing JSON: \(error)")
        }
    }
	
	private func openConnection() {
		if connectionState == .Connected {
			print("It's already connected.")
			return
		}
		
		// Make sure to replace the the IP to your local WIFI ip or localhost
		socket = WebSocket(request: URLRequest(url: URL(string: "ws://192.168.86.148:8080/ws/websocket")!))
		
		socket?.delegate = self
		socket?.connect()
		
		socket?.onEvent = { event in
			
		}
	}
	
	//IMPORTANT: This need to be called after socket is connect and before doing anything else
	// for supporting Spring Boot 2.1.x, or we won't get any event/callback
	private func sendConnectionHeader() {
		connectionHeaders = [StompCommands.commandHeaderAcceptVersion:"1.1,1.2"]
		self.sendFrame(command: StompCommands.commandConnect, header: connectionHeaders, body: nil)
	}
	
	public func subscribe(destination: String) {
		subscribeToDestination(destination: destination, ackMode: .AutoMode)
    }
	
	public func subscribeToDestination(destination: String, ackMode: StompAckMode) {
		print("subscribe to \(destination)")
		
		// This can be used unsubscribe `socketClient?.unsubscribe(destination: topicId)`
		// https://stackoverflow.com/a/48769453
		let topicId = UUID().uuidString
		var headers = [StompCommands.commandHeaderDestinationId: topicId, StompCommands.commandHeaderDestination: destination]
		
        var ack = ""
        switch ackMode {
        case StompAckMode.ClientMode:
            ack = StompCommands.ackClient
            break
        case StompAckMode.ClientIndividualMode:
            ack = StompCommands.ackClientIndividual
            break
        default:
            ack = StompCommands.ackAuto
            break
        }
		
		headers[StompCommands.commandHeaderAck] = ack
        self.sendFrame(command: StompCommands.commandSubscribe, header: headers, body: nil)
    }
    
    public func subscribeWithHeader(destination: String, withHeader header: [String: String]) {
        var headerToSend = header
        headerToSend[StompCommands.commandHeaderDestination] = destination
        sendFrame(command: StompCommands.commandSubscribe, header: headerToSend, body: nil)
    }
	
    private func sendFrame(command: String?, header: [String: String]?, body: AnyObject?) {
		guard let socket = socket, connectionState == .Connected else {
			return
		}
		
		var frameString = ""
		if command != nil {
			frameString = command! + "\n"
		}
		
		if let header = header {
			for (key, value) in header {
				frameString += key
				frameString += ":"
				frameString += value
				frameString += "\n"
			}
		}
		
		if let body = body as? String {
			frameString += "\n"
			frameString += body
		} else if let _ = body as? NSData {
			
		}
		
		if body == nil {
			print("inx append \n")
			frameString += "\n"
		}
		
		frameString += StompCommands.controlChar
		
		print("\nSend STOMP message:\(frameString)")
		socket.write(string: frameString) {
			print("inx complete")
		}
    }
}

extension StompClient: WebSocketDelegate {
	
	func didReceive(event: WebSocketEvent, client: WebSocket) {
		print("didReceive event:\(event)")
		
		switch event {
		case .connected(let headers):
			print("Websocket is connected: \(headers)")
			connectionState = .Connected
			// Support for Spring Boot 2.1.x
			sendConnectionHeader()
			subscribe(destination: "/topic/greeting")
		case .disconnected(let reason, let code):
			print("Websocket is disconnected: \(reason) with code: \(code)")
			connectionState = .Disconnected
		case .text(let string):
			print("Received text: \(string)")
		case .binary(let data):
			print("Received data: \(data.count)")
		case .ping(_):
			break
		case .pong(_):
			break
		case .viabilityChanged(_):
			break
		case .reconnectSuggested(_):
			break
		case .cancelled:
			break
		case .error(let error):
			print("error:\(error.debugDescription)")
		}
	}
}

