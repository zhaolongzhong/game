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
	
	// MARK: - Properties
	private let helloWorldLabel: UILabel = {
		let label = UILabel()
		label.translatesAutoresizingMaskIntoConstraints = false
		label.isUserInteractionEnabled = true
		label.textColor = UIColor.black
		label.textAlignment = NSTextAlignment.center
		label.font = UIFont.systemFont(ofSize: 14)
		label.text = "Hello World"
		return label
	}()
	
	private var socket: StompClient?
	private var pingTimer: Timer? {
		willSet { pingTimer?.invalidate() }
	}
	
	override func viewDidLoad() {
		super.viewDidLoad()
		// Do any additional setup after loading the view.
		
		view.addSubview(helloWorldLabel)
		
		helloWorldLabel.widthAnchor.constraint(equalToConstant: 200.0).isActive = true
		helloWorldLabel.heightAnchor.constraint(equalToConstant: 40.0).isActive = true
		helloWorldLabel.centerXAnchor.constraint(equalTo: view.centerXAnchor).isActive = true
		helloWorldLabel.centerYAnchor.constraint(equalTo: view.centerYAnchor).isActive = true
		
		let helloWorldGR = UITapGestureRecognizer(target: self, action: #selector(ViewController.helloWorldOnTapped))
        helloWorldLabel.addGestureRecognizer(helloWorldGR)
		
		socket = StompClient(socketDelegate: self)
		socket?.openConnection()
	}
	
	deinit {
		pingTimer = nil // cancel ping timer
	}
	
	@objc private func helloWorldOnTapped() {
		sendGreeting()
		sendChat()
	}
	
	func sendChat() {
		print("SendChat")
		let chatTopic = "/topic/chat"
		socket?.sendJSONForDict(dict: ["userId":"id_ios_123", "message": "hello, this is an iOS user"] as AnyObject, toDestination: chatTopic)
	}
	
	func sendGreeting() {
		print("SendGreeting")
		
		let greetingTopic = "/topic/greeting"
		var headerToSend = [String: String]()
		headerToSend[StompCommands.commandHeaderDestination] = greetingTopic
		socket?.sendFrame(command: StompCommands.commandSend, header: headerToSend, body: "hello from iOS" as AnyObject)
	}
}

extension ViewController: SocketDelegate {
	func didConnected() {
		schedulePingTimer()
		subscribeTopics()
	}
	
	func subscribeTopics() {
		socket?.subscribe(destination: "/topic/greeting")
		socket?.subscribe(destination: "/topic/chat")
	}
	
	private func schedulePingTimer() {
		let waitTime: TimeInterval = 5
		print("schedulePingTimer waitTime: 5s")
		DispatchQueue.main.async {
			self.pingTimer = Timer.scheduledTimer(timeInterval: waitTime, target: self, selector: #selector(self.pingTimerFired), userInfo: nil, repeats: true)
		}
	}
	
	@objc func pingTimerFired() {
		socket?.sendPing()
	}
}
