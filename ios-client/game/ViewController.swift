//
//  ViewController.swift
//  game
//
//  Created by Zhaolong Zhong on 6/21/20.
//  Copyright © 2020 Zhaolong Zhong. All rights reserved.
//

import UIKit
import Starscream

class ViewController: UIViewController {
	
    private static func createLabel(text: String) -> UILabel {
        let label = UILabel()
        label.translatesAutoresizingMaskIntoConstraints = false
        label.isUserInteractionEnabled = true
        label.textColor = UIColor.black
        label.textAlignment = NSTextAlignment.center
        label.font = UIFont.systemFont(ofSize: 14)
        label.text = text
        return label
    }
    
	// MARK: - Properties
    private let connectLabel: UILabel = createLabel(text: "Connect WebSocket")
    private let sendGreetingLabel: UILabel = createLabel(text: "Send Greeting")
    private let sendMessageLabel: UILabel = createLabel(text: "Send Message")
    private let disconectLabel: UILabel = createLabel(text: "Disconnect WebSocket")
    private let statusLabel: UILabel = createLabel(text: "Hello World!")
    
    private let stackView: UIStackView = {
        let stackView = UIStackView()
        stackView.axis = .vertical
        stackView.spacing = 10
        stackView.translatesAutoresizingMaskIntoConstraints = false
        return stackView
    }()
	
	private var socket: StompClient?
	private var pingTimer: Timer? {
		willSet { pingTimer?.invalidate() }
	}
	
	override func viewDidLoad() {
		super.viewDidLoad()
		// Do any additional setup after loading the view.
		
		view.addSubview(stackView)
        view.addSubview(statusLabel)
        
        stackView.addArrangedSubview(connectLabel)
        stackView.addArrangedSubview(sendGreetingLabel)
        stackView.addArrangedSubview(sendMessageLabel)
        stackView.addArrangedSubview(disconectLabel)
		
        stackView.widthAnchor.constraint(equalToConstant: 200.0).isActive = true
        stackView.centerXAnchor.constraint(equalTo: view.centerXAnchor).isActive = true
        stackView.centerYAnchor.constraint(equalTo: view.centerYAnchor).isActive = true
        
        statusLabel.topAnchor.constraint(equalTo: stackView.bottomAnchor, constant: 20).isActive = true
        statusLabel.widthAnchor.constraint(equalToConstant: 300.0).isActive = true
        statusLabel.centerXAnchor.constraint(equalTo: view.centerXAnchor).isActive = true
        
        statusLabel.numberOfLines = 10

        // Set up tap events
		let connectTGR = UITapGestureRecognizer(target: self, action: #selector(ViewController.connectLabelOnTapped))
        let sendGreetingTGR = UITapGestureRecognizer(target: self, action: #selector(ViewController.sendGreetingLabelOnTapped))
        let sendMessageTGR = UITapGestureRecognizer(target: self, action: #selector(ViewController.sendMessageLabelOnTapped))
        let disconnectTGR = UITapGestureRecognizer(target: self, action: #selector(ViewController.disconnectLabelOnTapped))
        
        connectLabel.addGestureRecognizer(connectTGR)
        sendGreetingLabel.addGestureRecognizer(sendGreetingTGR)
        sendMessageLabel.addGestureRecognizer(sendMessageTGR)
        disconectLabel.addGestureRecognizer(disconnectTGR)
	}
    
    private func initWebSocket() {
        guard socket == nil else {
            return
        }
        socket = StompClient(socketDelegate: self)
        socket?.openConnection()
    }
    
    private func closeConnection() {
        socket?.closeConnection()
        socket = nil
        updateStatus("Disconnected")
    }
    
    private func updateStatus(_ message: String) {
        DispatchQueue.main.async {
            self.statusLabel.text = message
        }
    }
	
	deinit {
		pingTimer = nil // cancel ping timer
	}
	
	@objc private func connectLabelOnTapped() {
        initWebSocket()
	}
    
    @objc private func sendGreetingLabelOnTapped() {
        sendGreeting()
    }
    
    @objc private func sendMessageLabelOnTapped() {
        sendChat()
    }
    
    @objc private func disconnectLabelOnTapped() {
        closeConnection()
    }
	
	private func sendChat() {
		print("SendChat")
		let chatTopic = "/topic/chat"
		socket?.sendJSONForDict(dict: ["userId":"id_ios_123", "message": "A greeting from an iOS user at \(Date())"] as AnyObject, toDestination: chatTopic)
	}
	
	private func sendGreeting() {
		print("SendGreeting")
		
		let greetingTopic = "/topic/greeting"
		var headerToSend = [String: String]()
		headerToSend[StompCommands.commandHeaderDestination] = greetingTopic
		socket?.sendFrame(command: StompCommands.commandSend, header: headerToSend, body: "A message from iOS user at \(Date())" as AnyObject)
	}
}

extension ViewController: SocketDelegate {
	func didConnected() {
        print("didConnected")
		schedulePingTimer()
		subscribeTopics()
	}
    
    func onEvent(event: WebSocketEvent) {
        updateStatus("\(event)")
    }
	
	func subscribeTopics() {
		socket?.subscribe(destination: "/topic/greeting")
		socket?.subscribe(destination: "/topic/chat")
	}
	
	private func schedulePingTimer() {
		let waitTime: TimeInterval = 10
		print("schedulePingTimer waitTime: 10s")
		DispatchQueue.main.async {
			self.pingTimer = Timer.scheduledTimer(timeInterval: waitTime, target: self, selector: #selector(self.pingTimerFired), userInfo: nil, repeats: true)
		}
	}
	
	@objc func pingTimerFired() {
		socket?.sendPing()
	}
}
