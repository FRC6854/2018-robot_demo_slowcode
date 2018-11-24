package org.usfirst.frc.team6854.robot;


import com.ctre.phoenix.motorcontrol.ControlMode;
import com.ctre.phoenix.motorcontrol.can.TalonSRX;
import edu.wpi.first.wpilibj.CameraServer;
import edu.wpi.first.wpilibj.DigitalInput;
import edu.wpi.first.wpilibj.DriverStation;
import edu.wpi.first.wpilibj.Encoder;
import edu.wpi.first.wpilibj.IterativeRobot;
import edu.wpi.first.wpilibj.Joystick;
import edu.wpi.first.wpilibj.Solenoid;
import edu.wpi.first.wpilibj.Spark;
import edu.wpi.first.wpilibj.SpeedControllerGroup;
import edu.wpi.first.wpilibj.drive.DifferentialDrive;
import edu.wpi.first.wpilibj.smartdashboard.SendableChooser;
import edu.wpi.first.wpilibj.smartdashboard.SmartDashboard;

public class Robot extends IterativeRobot {
	//  *****************************Object and variable declarations*********************
	//Drive declarations 
	Spark leftFrontdrive = new Spark(2); // 2 = PWM Output
	Spark leftReardrive = new Spark(1); // 1 = PWM Output
	Spark rightFrontdrive = new Spark(4); // 4 = PWM Output
	Spark rightReardrive = new Spark(3); // 3 = PWM Output
	SpeedControllerGroup leftDrive = new SpeedControllerGroup(leftFrontdrive, leftReardrive); //Groups the left two motors
	SpeedControllerGroup rightDrive = new SpeedControllerGroup(rightFrontdrive, rightReardrive); //Groups the right two motors
	DifferentialDrive drive = new DifferentialDrive(leftDrive, rightDrive); //Groups the left and right groups 

	//Talon declarations
	TalonSRX lift = new TalonSRX(1); //Connected to CAN Bus
	TalonSRX leftClampmotor = new TalonSRX(2); // ^ 
	TalonSRX rightClampmotor = new TalonSRX(3);// ^

	//Lift declarations
	double liftHeight = 0;
	double liftHoldPercent = 0.1;
	double percentHeight = 0;
	double autoStepDelay = 1000; // 500

	//Encoder declarations
	Encoder liftHeightcount = new Encoder(0, 1, false, Encoder.EncodingType.k4X); // 0, 1 = digital inputs, false = sets the polarity of the count, k4x = 4x accuracy is obtained

	//Switch declarations
	DigitalInput bottomLimitswitch = new DigitalInput(2);
	DigitalInput halfLimitswitch = new DigitalInput(3);
	DigitalInput topLimitswitch = new DigitalInput(4);

	//Solenoid declarations and variables
	Solenoid clampPinch = new Solenoid(4);//Connected to pnematic controller 
	Solenoid clampHeight = new Solenoid(1);//Connected to pnematic controller 
	Solenoid clampMovement = new Solenoid(2);//Connected to pnematic controller 

	boolean clampLower = false;
	boolean upButtonpressed = false;
	double upButtonlastTime = 0;

	boolean clampRelease = false;
	boolean clampButtonpressed = false;
	double clampButtonlastTime = 0;

	boolean clampForward = false;
	boolean clampForwardbuttonPressed = false;
	double clampForwardbuttonLasttime = 0;

	//Game data and selection variables
	String gameData = "";
	String startSide = "";
	SendableChooser<String> autoChooser = new SendableChooser(); // allows for user selection in dashboard
	SendableChooser<String> liftEnable = new SendableChooser();

	//Automous Variables
	double lastTime = 0;
	boolean autoComplete = false;

	//Controller declarations
	Joystick mainController = new Joystick(0); // 0 = 1st controller in Drive Station list
	Joystick secController = new Joystick(1); // 1 = 2nd controller in Drive Station list

	@Override
	// *********************************** Robot Init *********************************
	public void robotInit() { // Only runs once when the robot is enabled in any mode

		
		CameraServer.getInstance().startAutomaticCapture(); //Adds the camera

		liftHeightcount.reset(); // resets the encoder value to 0 

		clampPinch.set(clampRelease);//Sets default state
		clampHeight.set(clampLower);//^
		clampMovement.set(clampForward);//^

		//Smart Dashboard for start side
		autoChooser.addDefault("Left", "L"); //L
		autoChooser.addObject("Center", "C"); // C
		autoChooser.addObject("Right", "R"); //R
		
		liftEnable.addObject("On", "y");
		liftEnable.addObject("Off", "n");
		SmartDashboard.putData("Start Position", autoChooser);
		SmartDashboard.putData("Lift Enable", liftEnable);

	}

	@Override
	//  *********************   Auto Initialize   ******************************
	public void autonomousInit() { // Runs once when autonomous is enabled

		// NOTE leftDrive & rightDrive are reversed **************************************************************
		// Talk to Mr. Helleman April 8, 2018

		
		startSide = autoChooser.getSelected();
		clampPinch.set(false); //Keep the pincher closed when auto is started
		clampHeight.set(false); //Keep the clamp height high
		//Smart Dashboard
		SmartDashboard.putString("Clamp Status", "Autonomous Init Pinch False Height False");

		autoComplete = false;


		gameData = "";
		
		
		lastTime = System.currentTimeMillis();
	}

	@Override
	//  *********************   Auto    ******************************
	public void autonomousPeriodic() { // This loops automatically during autonomous

		//startSide = "C";	
		while (gameData.length() != 3) {
			gameData = DriverStation.getInstance().getGameSpecificMessage();
		}
		SmartDashboard.putString("Start Side Auto", startSide);
		SmartDashboard.putString("GameData Auto", gameData);

		if (autoComplete == false) {
			autoComplete = true;

			//Go Up Does not matter which position
			lastTime = System.currentTimeMillis(); // Resets timer
			while ((System.currentTimeMillis() - lastTime) < 1500) { // Lift Up
				lift.set(ControlMode.PercentOutput, 0.5);
			}

			lastTime = System.currentTimeMillis(); // Resets timer
			while ((System.currentTimeMillis() - lastTime) < autoStepDelay) { // delay and Hold Height
				lift.set(ControlMode.PercentOutput, liftHoldPercent);
			}

			// Left Side ***************************************************************** Left Side
			if (startSide == "L") {
				System.out.println("Left Side");

				//Go forward for 3.5 seconds
				lastTime = System.currentTimeMillis(); // Resets timer
				while ((System.currentTimeMillis() - lastTime) < 1700) { // Forward 2500
					rightDrive.set(0.5);
					leftDrive.set(-0.5);
				}

				lastTime = System.currentTimeMillis(); // Resets timer
				while ((System.currentTimeMillis() - lastTime) < autoStepDelay) { // Stop &  Wait
					rightDrive.set(0);
					leftDrive.set(0);
				}

				if (gameData.charAt(0) == 'L') { //If we own it, turn right, drive & drop
					System.out.println("Left Side & We OWN it so turn right");
					//Turn right
					lastTime = System.currentTimeMillis(); // Resets timer
					while ((System.currentTimeMillis() - lastTime) < 2000) { // Turn Right
						//leftDrive.set(0.5);
						rightDrive.set(0.7);
					}

					// Delay
					lastTime = System.currentTimeMillis(); // Resets timer
					while ((System.currentTimeMillis() - lastTime) < autoStepDelay) { // Stop & Pause
						//leftDrive.set(0);
						rightDrive.set(0.0);
					}

					//Go forward
					lastTime = System.currentTimeMillis(); // Resets timer
					while ((System.currentTimeMillis() - lastTime) < 1000) { // Drive Forward				  
						rightDrive.set(0.5);
						leftDrive.set(-0.5);

					}

					//Release cube
					lastTime = System.currentTimeMillis(); // Resets timer
					while ((System.currentTimeMillis() - lastTime) < 1000) { // Release Cube
						clampMovement.set(true);
					}

					lastTime = System.currentTimeMillis(); // Resets timer
					while ((System.currentTimeMillis() - lastTime) < 1000) { // Release Cube
						rightDrive.set(0);
						leftDrive.set(0);
						clampPinch.set(true);
						leftClampmotor.set(ControlMode.PercentOutput, 0.5);
						rightClampmotor.set(ControlMode.PercentOutput, 0.5);
					}
					lastTime = System.currentTimeMillis(); // Resets timer
					while ((System.currentTimeMillis() - lastTime) < autoStepDelay) { // 
						leftClampmotor.set(ControlMode.PercentOutput, 0);
						rightClampmotor.set(ControlMode.PercentOutput, 0);
					}

				}

			}

			// Right Side ***************************************************************** Right Side
			if (startSide == "R") {
				System.out.println("Right Side");

				//Go forward for 3.5 seconds
				lastTime = System.currentTimeMillis(); // Resets timer
				while ((System.currentTimeMillis() - lastTime) < 1700) { // Forward
					rightDrive.set(0.5);
					leftDrive.set(-0.5);
				}

				lastTime = System.currentTimeMillis(); // Resets timer
				while ((System.currentTimeMillis() - lastTime) < autoStepDelay) { // Stop &  Wait
					rightDrive.set(0);
					leftDrive.set(0);
				}

				if (gameData.charAt(0) == 'R') { //If we own it, turn left, drive & drop
					System.out.println("Right Side & We OWN it so turn Left");
					//Turn Left
					lastTime = System.currentTimeMillis(); // Resets timer
					while ((System.currentTimeMillis() - lastTime) < 2000) { // Turn Left
						leftDrive.set(-0.7);
						// rightDrive.set(0.5);
					}

					// Delay
					lastTime = System.currentTimeMillis(); // Resets timer
					while ((System.currentTimeMillis() - lastTime) < autoStepDelay) { // Stop & Pause
						leftDrive.set(0);
						// rightDrive.set(0.0);
					}

					//Go forward
					lastTime = System.currentTimeMillis(); // Resets timer
					while ((System.currentTimeMillis() - lastTime) < 1000) { // Drive Forward				  
						rightDrive.set(0.5);
						leftDrive.set(-0.5);
					}

					//Release cube
					lastTime = System.currentTimeMillis(); // Resets timer
					while ((System.currentTimeMillis() - lastTime) < 1000) { // Release Cube
						clampMovement.set(true);
					}

					lastTime = System.currentTimeMillis(); // Resets timer
					while ((System.currentTimeMillis() - lastTime) < 1000) { // Release Cube
						rightDrive.set(0);
						leftDrive.set(0);
						clampPinch.set(true);
						leftClampmotor.set(ControlMode.PercentOutput, 0.5);
						rightClampmotor.set(ControlMode.PercentOutput, 0.5);
					}
					lastTime = System.currentTimeMillis(); // Resets timer
					while ((System.currentTimeMillis() - lastTime) < autoStepDelay) { // 
						leftClampmotor.set(ControlMode.PercentOutput, 0);
						rightClampmotor.set(ControlMode.PercentOutput, 0);
					}

				}
			}

			// Center ***************************************************************** Center
			if (startSide == "C") {
				System.out.println("Center");
				// Delay
				lastTime = System.currentTimeMillis(); // Resets timer
				while ((System.currentTimeMillis() - lastTime) < 2000) { // Stop & Pause
					leftDrive.set(0);
					rightDrive.set(0);
				}

				if (gameData.charAt(0) == 'L') {
					System.out.println("Center we own Left");
					lastTime = System.currentTimeMillis(); // Resets timer
					while ((System.currentTimeMillis() - lastTime) < 500) { // Turn Left
						leftDrive.set(-0.65);
						// rightDrive.set(0.5);
					}

					// Delay
					lastTime = System.currentTimeMillis(); // Resets timer
					while ((System.currentTimeMillis() - lastTime) < autoStepDelay) { // Stop & Pause
						leftDrive.set(0);
						// rightDrive.set(0.0);
					}
				}

				if (gameData.charAt(0) == 'R') { //Run right motor for 10 seconds
					System.out.println("Center we own Right");
					lastTime = System.currentTimeMillis(); // Resets timer
					while ((System.currentTimeMillis() - lastTime) < 500) { // Turn Left
						// leftDrive.set(-0.5);
						rightDrive.set(0.6);
					}

					// Delay
					lastTime = System.currentTimeMillis(); // Resets timer
					while ((System.currentTimeMillis() - lastTime) < autoStepDelay) { // Stop & Pause
						// leftDrive.set(0);
						rightDrive.set(0.0);
					}

				}
				//Go forward
				lastTime = System.currentTimeMillis(); // Resets timer
				while ((System.currentTimeMillis() - lastTime) < 2200) { // Drive Forward				  
					rightDrive.set(0.5);
					leftDrive.set(-0.5);
				}
				//Delay
				lastTime = System.currentTimeMillis(); // Resets timer
				while ((System.currentTimeMillis() - lastTime) < 1000) { // delay
					;
				}

				//Release cube
				lastTime = System.currentTimeMillis(); // Resets timer
				while ((System.currentTimeMillis() - lastTime) < 1000) { // Release Cube
					clampMovement.set(true);
				}

				lastTime = System.currentTimeMillis(); // Resets timer
				while ((System.currentTimeMillis() - lastTime) < 1000) { // Release Cube
					rightDrive.set(0);
					leftDrive.set(0);
					clampPinch.set(true);
					leftClampmotor.set(ControlMode.PercentOutput, 0.5);
					rightClampmotor.set(ControlMode.PercentOutput, 0.5);
				}
				lastTime = System.currentTimeMillis(); // Resets timer
				while ((System.currentTimeMillis() - lastTime) < autoStepDelay) { // 
					leftClampmotor.set(ControlMode.PercentOutput, 0);
					rightClampmotor.set(ControlMode.PercentOutput, 0);
				}

			}
		}

	}

	//	**********************************************************************************************************
	//  ***************************************** Teleop Code*****************************************************
	//  **********************************************************************************************************
	/*
	 * public void teleopInit() { clampPinch.set(true); //Keep the pincher closed
	 * when auto is started clampHeight.set(false); //Keep the clamp height high }
	 */
	//	****************************************Teleop Loop******************************************************
	@Override
	public void teleopPeriodic() { // This loops automatically during teleop
		// Raw count of 37000 = 69"
		liftHeight = liftHeightcount.get() * .0018649; /// / 2000 * 2 * 3.14 * 0.75; // Gets the value from the encoder then divides it by the number of counts per revolution and then multiplies by the circumference of the rope spool
		percentHeight = Math.abs(1 - (liftHeight / 300)); // the denominator should be larger than the actual max height of the lift to keep drive at 50% power
		if (percentHeight > 1) { // Ensure value is not greater than 100%
			percentHeight = 1;
		}
		// Future updates add .5 to value to ensure minimum is greater than 50% not / 200 use /70

		//  ******************************** Smart Dashbaord ****************************************
		SmartDashboard.putNumber("Lift Height (inches) ", liftHeight);

		SmartDashboard.putNumber("Lift Percent ", percentHeight);

		SmartDashboard.putNumber("Encoder RAW", liftHeightcount.get);
		
		System.out.println(liftHeight);

		//  ******************************  Drive Code  *********************************************

		// ***** Arcade Drive*****
		// Based on height of lift max speed will be limited by percentage height
		// Multiplied by 0.5 to slow it down for demo
		
		drive.arcadeDrive(mainController.getRawAxis(1) * percentHeight * 0.5, mainController.getRawAxis(4) * percentHeight * 0.5); // Arcade drive:gets axis from controller and sends it to motors

		//  ******************************  Lift Code  **********************************************
		
		if(liftEnable.getSelected == "y"){
			if (liftHeight < 36){		// Keep it low for demo to avoid tipping
				if (mainController.getRawAxis(3) > 0 || mainController.getRawAxis(2) > 0) { //3 = right trigger up, 2 = left trigger 
					if (mainController.getRawAxis(3) > 0 && (halfLimitswitch.get() == true || topLimitswitch.get() == true)) { //If the right trigger is recessed and the half and top limit switches are not pressed 
						lift.set(ControlMode.PercentOutput, mainController.getRawAxis(3)); //Move up based on the recession of the right trigger
					} else {
						lift.set(ControlMode.PercentOutput, 0);
					}
					if (mainController.getRawAxis(2) > 0 && bottomLimitswitch.get() == true) { //If the right trigger is recessed and the bottom switch is not pressed
						if (liftHeight < 6) { //Going down below 20 inches
							lift.set(ControlMode.PercentOutput, (mainController.getRawAxis(2) * -1) * 0.5); // was .2   Move down based on the recession of the right trigger
						} else {
							lift.set(ControlMode.PercentOutput, mainController.getRawAxis(2) * -1); //Move down based on the recession of the right trigger
						}
					}
				} else {
					if (liftHeight > 20) { //If the lift is higher than 20 inches
						lift.set(ControlMode.PercentOutput, 0.1); //Run the lift motor at 10% to keep it up
					} else {
						lift.set(ControlMode.PercentOutput, 0); //Otherwise do not run motor
					}
				}
			}	
		}
		
		// ******************************  Solenoids *********************************************

		// This block creates a one shot toggle for the Clamp Raise
		if ((mainController.getRawButton(5) || secController.getRawButton(5)) && !upButtonpressed) { //5 = button on controller !upButton sets that bool to true
			upButtonpressed = true; //This creates a one shot
			clampLower = !clampLower; //This inverts the raise status
		}
		if ((mainController.getRawButton(5) || secController.getRawButton(5))) { //Keps time to 0 when pressed
			upButtonlastTime = System.currentTimeMillis();
		}
		if ((System.currentTimeMillis() - upButtonlastTime) > 50) { //Reset the button when it is let go
			upButtonpressed = false;
		}

		// This block creates a one shot toggle for the Clamp Release
		if ((mainController.getRawButton(6) || secController.getRawButton(6)) && !clampButtonpressed) { //6 = button on controller !upButton sets that bool to true
			clampButtonpressed = true; //This creates a one shot
			clampRelease = !clampRelease; //This inverts the clamp status
		}
		if ((mainController.getRawButton(6) || secController.getRawButton(6))) { //Keps time to 0 when pressed
			clampButtonlastTime = System.currentTimeMillis();
		}
		if ((System.currentTimeMillis() - clampButtonlastTime) > 50) { //Reset the button when it is let go
			clampButtonpressed = false;
		}

		// This block creates a one shot toggle for the Clamp Forward/Backwards
		if ((mainController.getRawButton(4) || secController.getRawButton(4)) && !clampForwardbuttonPressed) { //6 = button on controller !upButton sets that bool to true
			clampForwardbuttonPressed = true; //This creates a one shot
			clampForward = !clampForward; //This inverts the clamp status
		}
		if ((mainController.getRawButton(4) || secController.getRawButton(4))) { //Keps time to 0 when pressed
			clampForwardbuttonLasttime = System.currentTimeMillis();
		}
		if ((System.currentTimeMillis() - clampForwardbuttonLasttime) > 50) { //Reset the button when it is let go
			clampForwardbuttonPressed = false;
		}

		//clamp Motors
		leftClampmotor.set(ControlMode.PercentOutput, (secController.getRawAxis(1) / 2) * -1);
		rightClampmotor.set(ControlMode.PercentOutput, (secController.getRawAxis(5) / 2) * -1);

		//Sets the solenoids to the value of the toggles
		clampPinch.set(clampRelease);
		clampHeight.set(clampLower);
		clampMovement.set(clampForward);
	}

	@Override
	public void testPeriodic() {
		//Used for running air compresser only ATM
	}
}
