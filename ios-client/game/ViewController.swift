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
		
		socket = StompClient()
		socket?.testWebsocket()
	}
	
	@objc private func helloWorldOnTapped() {
		socket?.sendGreeting()
	}
}
