/**
 *  Aeon Home Energy Meter v2 Gen2 Basic Edition
 *	Version: 0.9b
 *
 *	Disclaimer: This WILL NOT work with Aeon's HEM Gen1 or Gen5 (latest version) as is intended to be used for HEMs
 *				installed on the typical 200A 240V split-phase US residential systems (Two 120V legs and a neutral -
 *				grounded center tap from power transformer outside your house).
 *
 *  Copyright 2016 Alex M. Ruffell
 *
 *  Licensed under the Apache License, Version 2.0 (the "License"); you may not use this file except
 *  in compliance with the License. You may obtain a copy of the License at:
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software distributed under the License is distributed
 *  on an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the License
 *  for the specific language governing permissions and limitations under the License.
 *
 *  Author: Alex M. Ruffell
 *
 *  Note:	This device handler is based off of Smartthings' "Aeon Smart Meter" device handler sample code but mostly on
 *			a device handler written by Barry A. Burke (Thank you!!). I have made very significant changes to his code,
 *			stripped stuff, and added other stuff but the overall structure likely remains (for now at least).
 *
 *	Goal:	I removed all support for v1 (sorry!) to keep the code simple and pertinent to what I thought was the latest
 *			version of the HEM - v2, but I just realized Aeon released Gen5 with zwave plus. I wanted to have a device
 *			handler that fully supported Android as I would never even dream of buying any Apple products! I also wanted
 *			a way to keep track of my usage to get the best deal out of my energy provier's contract. Calculating cost
 *			was of no use to me given the price per kWh changes based on my consumption. In my case, if I am between
 *			1000kWh and 2000kWh in a sinlgle month, I get a $100 credit on my account so this device handler just has a
 *			counter you can reset monthly. Last, I removed one of the digits after the decimal as it is generally superfluos
 *			for typical HEM applications and it is also likely that the accuracy of the HEM doesn't guarantee the accuracy of
 *			the measurement anyway (at least not over time given there is no calibration feature).
 *
 *	To Do:	- Possibly add back some of the min/max functionality but I am not sure how useful that would be
 *			- Add preference to switch from kWh to kVAh
 *			- Enable Debug on/off preference to declutter logs when debugging is not needed
 *			- Once debug on/off is enabled, add more debugging to help troubleshoot issues
 *			- Leg 1 and Leg 2 voltage measurement is not being read. Figure out configuration settings to make it work
 *			  even though I am not displaying the values
 *			- Check whether polling is needed, and enable if needed
 *			- Refresh and Configure button may not be necessary, evaluate and leave/remove as needed
 *			- Figure out whether I am making the best use of the capability defined attributes, or can I just ignore them?
 *			- Figure out Reporting Group 1, 2, and 3 - what is sent and how often. Goal: reduce network congestion
 *			- Report delays may require delay values in Hexadecimal so passing 120s might require entering 78. Reports seem to run too frequently
 *			- Figure out why at times the values in the tile are pushed down... ST bug or programming issue?
 *			- Why is tile text not resizing?
 *			- Tile color should go from green to yellow to red, rather than blue to red as values become to high or too low (only V).
 *			- Review foregroundColor and backgroundColor as it does not seem to be doing anything on some tiles. Fix or remove as needed.
 *			- My voltage measurement always seems a bit high, validate and add configurable +- offset to measured value
 *			- Verify W/A measurements using clamp meter on main panel. Add configurable +- offset to measured values
 *
 *
 *  History:
 * 		
 *	2016-07-15:	- Basic functionality seems to work but lots more work is necessary
 *
 *	Disclaimer 2:	I am NOT a developer. I learn as I go so please do NOT rely on this for anything critical. Use it and
 *					change it as needed but not for commercial purposes. I will not make any changes to this code that fix things on iOS
 *					if it breaks anything on Android - sorry! Also, I barely got to this point so adding new features may be out of my reach for now.
 *
 *
 */
metadata {
	definition (
		name: 		"Aeon HEMv2 Gen2 - AMR Basic Edition", 
		namespace: 	"Green Living",
		category: 	"Green Living",
		author: 	"Alex M. Ruffell",
        
        // ************************************************************************
    	// * Icons - for now random icons I found until I find a better fit
    	// ************************************************************************
    
    	iconUrl: "https://s3.amazonaws.com/smartapp-icons/Convenience/Cat-Convenience.png",
		iconX2Url: "https://s3.amazonaws.com/smartapp-icons/Convenience/Cat-Convenience@2x.png",
		iconX3Url: "https://s3.amazonaws.com/smartapp-icons/Convenience/Cat-Convenience@2x.png"
	)
    
	{
    	capability "Energy Meter"
		capability "Power Meter"
		capability "Configuration"
		capability "Sensor"
        capability "Refresh"
        //capability "Polling"

		attribute "energy",			"number"		// Sum of energy used on both legs, total energy used by house (defined by capability)
        attribute "power",			"number"		// Sum of power from both legs, total power used by house (defined by capability)
        attribute "amps",			"number"		// Sum of amperage from both legs, total power used by house (defined by capability)
        attribute "volts",			"number"		// Volts of both legs, total power used by house (defined by capability)
        
		attribute "E_L1_L2",		"string"		// Sum of energy (kWh) used on both legs, total energy used by house
        attribute "E_L1",			"string"		// Energy from leg 1
        attribute "E_L2",			"string"		// Energy from leg 2

        attribute "W_L1_L2",		"string"		// Sum of power from both legs, total power used by house
        attribute "W_L1",			"string"		// Power from leg 1
        attribute "W_L2",			"string"		// Power from leg 2
        
        attribute "V_L1_L2",		"string"		// Volts for leg 1 and 2 - voltage on L1 and L2 should always be the same, if not there is an issue!
        attribute "V_L1",			"string"		// Voltage on leg 1
        attribute "V_L2",			"string"		// Voltage on leg 2
        
        attribute "A_L1_L2",		"string"		// Sum of amerage used on both legs, total amperage used by house
        attribute "A_L1",			"string"		// Amperage for leg 1
        attribute "A_L2",			"string"        // Amperage for leg 2

        attribute "resetDate",		"string"		// Date kWh was reset. This helps keep track consumption is sync with power company meter readings
        
		command "reset"
        command "configure"
        command "refresh"
        command "resetCtr"
        //command "poll"

		// Fingerprint for Aeon HEMv2, Second Generation
        fingerprint deviceId: "0x3101", inClusters: "0x70,0x32,0x60,0x85,0x56,0x72,0x86"
	}

	// simulator metadata
	simulator {
		for (int i = 0; i <= 10000; i += 1000) {
			status "power  ${i} W": new physicalgraph.zwave.Zwave().meterV1.meterReport(
				scaledMeterValue: i, precision: 3, meterType: 33, scale: 2, size: 4).incomingMessage()
		}
		for (int i = 0; i <= 100; i += 10) {
			status "energy  ${i} kWh": new physicalgraph.zwave.Zwave().meterV1.meterReport(
				scaledMeterValue: i, precision: 3, meterType: 33, scale: 0, size: 4).incomingMessage()
		}
	}

    // ************************************************************************
    // * Tile Definitions
    // ************************************************************************
    
    // scale set to 2 so tiles can be set to anything between 1 and 6 wide
	tiles (scale:2) {
    
    // ************************************************************************
    // * Watts tiles
    // ************************************************************************
    
        valueTile("W_L1_L2", "device.W_L1_L2", width: 2, height: 2) {
			state (
				"W_L1_L2", 
                label:'${currentValue} W', 
            	foregroundColors:[
            		[value: 1, color: "#000000"],
            		[value: 10000, color: "#ffffff"]
            	], 
            	//foregroundColor: "#000000",
                backgroundColors:[
					[value: 0, 		color: "#006600"],	// Dark Green
					[value: 3000, 	color: "#009900"],	// Lighter Green
					[value: 6000, 	color: "#00cc00"],	// Light Green
					[value: 9000, 	color: "#99cc00"],	// Green, touch of yellow
					[value: 12000, 	color: "#ffcc00"],	// Yellow
					[value: 18000, 	color: "#ff6600"],	// Orange
					[value: 24000, 	color: "#ef221a"]	// Red
				
                	/* Original colors, taken from ST examples
                	[value: 0, 		color: "#153591"],
					[value: 3000, 	color: "#1e9cbb"],
					[value: 6000, 	color: "#90d2a7"],
					[value: 9000, 	color: "#44b621"],
					[value: 12000, 	color: "#f1d801"],
					[value: 18000, 	color: "#d04e00"], 
					[value: 24000, 	color: "#bc2323"]*/
				]
			)
		}
        valueTile("W_L1", "device.W_L1", width: 3, height: 2) {
        	state(
        		"W_L1", 
                label:'${currentValue} W',
            	foregroundColors:[
            		[value: 1, color: "#000000"],
            		[value: 10000, color: "#ffffff"]
            	], 
            	foregroundColor: "#000000",
                backgroundColors:[
					[value: 0, 		color: "#006600"],	// Dark Green
					[value: 3000, 	color: "#009900"],	// Lighter Green
					[value: 6000, 	color: "#00cc00"],	// Light Green
					[value: 9000, 	color: "#99cc00"],	// Green, touch of yellow
					[value: 12000, 	color: "#ffcc00"],	// Yellow
					[value: 18000, 	color: "#ff6600"],	// Orange
					[value: 24000, 	color: "#ef221a"]	// Red
				]
			)
        }
        valueTile("W_L2", "device.W_L2", width: 3, height: 2) {
        	state(
        		"W_L2", 
                label:'${currentValue} W', 
            	foregroundColors:[
            		[value: 1, color: "#000000"],
            		[value: 10000, color: "#ffffff"]
            	], 
            	foregroundColor: "#000000",
                backgroundColors:[
					[value: 0, 		color: "#006600"],	// Dark Green
					[value: 3000, 	color: "#009900"],	// Lighter Green
					[value: 6000, 	color: "#00cc00"],	// Light Green
					[value: 9000, 	color: "#99cc00"],	// Green, touch of yellow
					[value: 12000, 	color: "#ffcc00"],	// Yellow
					[value: 18000, 	color: "#ff6600"],	// Orange
					[value: 24000, 	color: "#ef221a"]	// Red
				]
			)
        }

	// ************************************************************************
    // * Energy tiles
    // ************************************************************************
    
		valueTile("resetDate", "device.resetDate", width: 3, height: 1) {
			state(
				"resetDate",
				label: '${currentValue}', 
				foregroundColor: "#000000", 
				backgroundColor: "#ffffff")
		}    
		valueTile("E_L1_L2", "device.E_L1_L2", width: 3, height: 1/*, canChangeIcon: true*/) {
			state(
				"E_L1_L2",
				label: '${currentValue} kWh', 
				foregroundColor: "#000000", 
				backgroundColor: "#ffffff")
		}
        valueTile("E_L1", "device.E_L1", width: 3, height: 1) {
        	state(
        		"E_L1",
        		label: '${currentValue} kWh', 
        		foregroundColor: "#000000", 
        		backgroundColor: "#ffffff")
        }        
        valueTile("E_L2", "device.E_L2", width: 3, height: 1) {
        	state(
        		"E_L2",
        		label: '${currentValue} kWh', 
        		foregroundColor: "#000000", 
        		backgroundColor: "#ffffff")
        }


        
	// ************************************************************************
    // * Voltage tile - Just one as voltage is the same on both legs
    // ************************************************************************
	
        valueTile("V_L1_L2", "device.V_L1_L2", width: 2, height: 2) {
        	state(
        		"V_L1_L2",
                label: '${currentValue} V', 
        		backgroundColors:[
            		[value: 115.6, 	color: "#ef221a"],
                	[value: 117.8, 	color: "#ffcc00"],
                	[value: 120.0, 	color: "#006600"],
                	[value: 122.2, 	color: "#ffcc00"],
                	[value: 124.4, 	color: "#ef221a"]
            	]
            )
        }

    
	// ************************************************************************
    // * Amperage tiles
    // ************************************************************************
    
        valueTile("A_L1_L2", "device.A_L1_L2", width: 2, height: 2) {
        	state (
        		"A_L1_L2",
                label: '${currentValue} A' , 
        		foregroundColor: "#000000", 
    			color: "#000000", 
    			backgroundColors:[
					[value: 0,		color: "#006600"],	// Dark Green
					[value: 25, 	color: "#009900"],	// Lighter Green
					[value: 50, 	color: "#00cc00"],	// Light Green
					[value: 75, 	color: "#99cc00"],	// Green, touch of yellow
					[value: 100,	color: "#ffcc00"],	// Yellow
					[value: 150,	color: "#ff6600"], 	// Orange
					[value: 200,	color: "#ef221a"]	// Red
				]
			)
        }
        valueTile("A_L1", "device.A_L1", width: 3, height: 2) {
        	state(
        		"A_L1",
                label:'${currentValue} A',
        		foregroundColor: "#000000",
    			color: "#000000",
    			backgroundColors:[
					[value: 0,		color: "#006600"],	// Dark Green
					[value: 25, 	color: "#009900"],	// Lighter Green
					[value: 50, 	color: "#00cc00"],	// Light Green
					[value: 75, 	color: "#99cc00"],	// Green, touch of yellow
					[value: 100,	color: "#ffcc00"],	// Yellow
					[value: 150,	color: "#ff6600"], 	// Orange
					[value: 200,	color: "#ef221a"]	// Red
				]
			)
        }
        valueTile("A_L2", "device.A_L2", width: 3, height: 2) {
        	state(
        		"A_L2",
                label:'${currentValue} A',
        		foregroundColor: "#000000", 
    			color: "#000000", 
    			backgroundColors:[
					[value: 0,		color: "#006600"],	// Dark Green
					[value: 25, 	color: "#009900"],	// Lighter Green
					[value: 50, 	color: "#00cc00"],	// Light Green
					[value: 75, 	color: "#99cc00"],	// Green, touch of yellow
					[value: 100,	color: "#ffcc00"],	// Yellow
					[value: 150,	color: "#ff6600"], 	// Orange
					[value: 200,	color: "#ef221a"]	// Red
				]
			)        		
        }
    
	// ************************************************************************
    // * CONTROL TILES
    // *
    // * Using decoration set to flat only because it enables visual feedback
    // * of button being pressed
    // * 
    // * Using background color as it makes it more obvious it is a button,
    // * and it delimites the space for text making it a bit more uniform
    // ************************************************************************
    
		// Reset Button - Clear display and start over (will not reset kWh counter)
        standardTile("reset", "command.reset", inactiveLabel: false, decoration: "flat", width: 2, height: 2) {
			state "default", label:'___reset___', action:"reset", icon: "st.Health & Wellness.health7", backgroundColor: "#bcccac"
		}
        
        // Reset kWh Button - Clear display, reset kWh counter and start over
        standardTile("resetCtr", "device.reset_ctr", inactiveLabel: false, decoration: "flat", width: 6, height: 2) {
			state "default", label:'reset kwh counter', action:"resetCtr", icon: "st.Office.office10"
		}
        
        // Refresh Button
		standardTile("refresh", "command.refresh", inactiveLabel: false, decoration: "flat", width: 2, height: 2) {
            state "default", label:'_refresh_', action:"refresh.refresh", icon:"st.secondary.refresh-icon", backgroundColor: "#bcccac" 
		}
        
        // Configure Button
		standardTile("configure", "command.configure", inactiveLabel: false, decoration: "flat", width: 2, height: 2) {
			state "configure", label:'configure', action:"configuration.configure", icon:"st.secondary.tools", backgroundColor: "#bcccac"
		}


	// ************************************************************************
    // * Which tiles are displayed and in what order
    // ************************************************************************

       	main ([
        	"E_L1_L2",
            "W_L1_L2",
            "A_L1_L2",
            "V_L1_L2"
            ])
        
		details([
			"V_L1_L2","W_L1_L2","A_L1_L2",
            "W_L1","W_L2",
            "A_L1","A_L2",
            "resetDate", "E_L1_L2",
            "E_L1","E_L2",
            "reset","refresh","configure",
            "resetCtr"
            ])
	}
    
	// ************************************************************************
    // * User selectable preferences for Device Handler
    // ************************************************************************
    
    // Setting a default value (defaultValue: "foobar") for an input may render that selection in the mobile app,
    // but the user still needs to enter data in that field. It’s recommended to not use defaultValue to avoid confusion.
    
    // Stuff disabled below is not fully functional yet
    
    preferences {
    	//input name: "energyMeasurement", type: "enum", title: "Energy meter measurement?", options: ["kWh", "kVAh"], description: "Select measurement type", required: true, displayDuringSetup: true
    	input "reportGroup1", type: "number", title: "Update energy meter every x seconds", description: "Enter desired seconds", defaultValue: 120, displayDuringSetup: false
    	input "reportGroup2", type: "number", title: "Update all values every x seconds", description: "Enter desired seconds", defaultValue: 60, displayDuringSetup: false
        input "reportGroup3", type: "number", title: "Update W & Total Power every x seconds", description: "Enter desired seconds", defaultValue: 10, displayDuringSetup: false
        //input "debugOnOff", type: "boolean", title: "Debug log messages", description: "", defaultValue: "off", displayDuringSetup: false
    }

}
	// ************************************************************************
    // * installed - to be reworked
    // ************************************************************************
    
def installed() {
	reset()						// The order here is important
	configure()					// Since reports can start coming in even before we finish configure()
    updateDisplay()
	refresh()
}

	// ************************************************************************
    // * updated - to be reworked
    // ************************************************************************

def updated() {
	configure()
	updateDisplay()
	refresh()
}

	// ************************************************************************
    // * parse
    // ************************************************************************

def parse(String description) {
//	log.debug "Parse received ${description}"
	def result = null
	def cmd = zwave.parse(description, [0x31: 1, 0x32: 1, 0x60: 3])
	if (cmd) {
		result = createEvent(zwaveEvent(cmd))
	}
	if (result) { 
		log.debug "Parse returned ${result?.descriptionText}"
		return result
	} else {
	}
}

	// ************************************************************************
    // * zwaveEvent
    // ************************************************************************
    
def zwaveEvent(physicalgraph.zwave.commands.meterv1.MeterReport cmd) {
    def newValue
    def formattedValue
    def MAX_AMPS = 220				// This exceeds typical residential split-phase panel amerage on purpose, cuts off values that are too high (fluke in reading)
    def MAX_WATTS = 26400			// This exceeds typical residential split-phase panel power on purpose, cuts off values that are too high (fluke in reading)
    
    // def timeStamp =  new Date().format("HH:MM - dd-MMM-yy", location.timeZone)
    
    if (cmd.meterType == 33) {
		if (cmd.scale == 0) {
        	newValue = Math.round(cmd.scaledMeterValue * 100) / 100
            formattedValue = String.format("%5.1f", newValue)
        	if (formattedValue != state.E_L1_L2) {
                sendEvent(name: "E_L1_L2", value: formattedValue, unit: "", descriptionText: "Display Energy: ${newValue} kWh", displayed: false)
                state.E_L1_L2 = formattedValue
                [name: "energy", value: newValue, unit: "kWh", descriptionText: "Total Energy: ${formattedValue} kWh"]
            }
		} 
		
        else if (cmd.scale == 1) {
            newValue = Math.round(cmd.scaledMeterValue * 100) / 100
            formattedValue = String.format("%5.1f", newValue)
            if (newValue != state.E_L1_L2) {
                sendEvent(name: "E_L1_L2", value: formattedValue, unit: "", descriptionText: "Display Energy: ${formattedValue} kVAh", displayed: false)
                state.E_L1_L2 = formattedValue
				[name: "energy", value: newValue, unit: "kVAh", descriptionText: "Total Energy: ${formattedValue} kVAh"]
            }
		}
        else if (cmd.scale==2) {				
        	newValue = Math.round(cmd.scaledMeterValue)
            formattedValue = newValue as String
            if (newValue > MAX_WATTS) { return }
        	if (formattedValue != state.W_L1_L2) {
                sendEvent(name: "W_L1_L2", value: formattedValue, unit: "", descriptionText: "Display Power: ${newValue} W", displayed: false)
                state.W_L1_L2 = formattedValue
                [name: "power", value: newValue, unit: "W", descriptionText: "Total Power: ${newValue} W"]
            }
		}
 	}
    else if (cmd.meterType == 161) {
    	if (cmd.scale == 0) {
        	newValue = Math.round(cmd.scaledMeterValue * 100) / 100
            formattedValue = String.format("%5.1f", newValue)
			if (formattedValue != state.V_L1_L2) {
                sendEvent(name: "V_L1_L2", value: formattedValue, unit: "", descriptionText: "Display Voltage: ${formattedValue} V", displayed: false)              
                state.V_L1_L2 = formattedValue
                [name: "volts", value: newValue, unit: "V", descriptionText: "Volts: ${formattedValue} V"]
            }
        }
        else if (cmd.scale==1) {
        	newValue = Math.round( cmd.scaledMeterValue * 100) / 100
            if (newValue > MAX_AMPS) { return }
            formattedValue = String.format("%5.1f", newValue)
            if (formattedValue != state.A_L1_L2) {
                sendEvent(name: "A_L1_L2", value: formattedValue, unit: "", descriptionText: "Display Current: ${formattedValue}", displayed: false)              
                state.A_L1_L2 = formattedValue
				[name: "amps", value: newValue, unit: "A", descriptionText: "Total Current: ${formattedValue} A"]
            }
        }
    }           
}

def zwaveEvent(physicalgraph.zwave.commands.multichannelv3.MultiChannelCmdEncap cmd) {
	def newValue
	def formattedValue
    def MAX_AMPS = 220				// This exceeds typical residential split-phase panel amerage on purpose, cuts off values that are too high (fluke in reading)
    def MAX_WATTS = 26400			// This exceeds typical residential split-phase panel power on purpose, cuts off values that are too high (fluke in reading)

   	if (cmd.commandClass == 50) {    
   		def encapsulatedCommand = cmd.encapsulatedCommand([0x30: 1, 0x31: 1]) // can specify command class versions here like in zwave.parse
		if (encapsulatedCommand) {
        	// This section handles values from Clamp 1 (EndPoint 1)
			if (cmd.sourceEndPoint == 1) {
				if (encapsulatedCommand.scale == 2 ) {
					newValue = Math.round(encapsulatedCommand.scaledMeterValue)
                    if (newValue > MAX_WATTS) { return }											//Ignore values that are too high, definitely incorrect
					formattedValue = newValue as String
					if (formattedValue != state.W_L1) {
						state.W_L1 = formattedValue
						[name: "W_L1", value: formattedValue, unit: "", descriptionText: "L1 Power: ${formattedValue} W"]
					}
				} 
				else if (encapsulatedCommand.scale == 0 ){
					newValue = Math.round(encapsulatedCommand.scaledMeterValue * 100) / 100
					formattedValue = String.format("%5.1f", newValue)
					if (formattedValue != state.E_L1) {
						state.E_L1 = formattedValue
						[name: "E_L1", value: formattedValue, unit: "", descriptionText: "L1 Energy: ${formattedValue} kWh"]
					}
				}
				else if (encapsulatedCommand.scale == 1 ){
					newValue = Math.round(encapsulatedCommand.scaledMeterValue * 100) / 100
					formattedValue = String.format("%5.1f", newValue)
					if (formattedValue != state.E_L1) {
						state.E_L1 = formattedValue
						[name: "E_L1", value: formattedValue, unit: "", descriptionText: "L1 Energy: ${formattedValue} kVAh"]
					}
				}
				else if (encapsulatedCommand.scale == 5 ) {
					newValue = Math.round(encapsulatedCommand.scaledMeterValue * 100) / 100
                    if (newValue > MAX_AMPS) { return }												//Ignore values that are too high, definitely incorrect
                    formattedValue = String.format("%5.1f", newValue)
					if (formattedValue != state.A_L1) {
						state.A_L1 = formattedValue
						[name: "A_L1", value: formattedValue, unit: "", descriptionText: "L1 Current: ${formattedValue} A"]
					}
               	} 
              	else if (encapsulatedCommand.scale == 4 ){
               		newValue = Math.round(encapsulatedCommand.scaledMeterValue * 100) / 100
					formattedValue = String.format("%5.1f", newValue)
					if (formattedValue != state.V_L1) {
						state.V_L1 = formattedValue
						[name: "V_L1", value: formattedValue, unit: "", descriptionText: "L1 Voltage: ${formattedValue} V"]
					}
               	}               
			}
            // This section handles values from Clamp 2 (EndPoint 2)
			else if (cmd.sourceEndPoint == 2) {
				if (encapsulatedCommand.scale == 2 ){
					newValue = Math.round(encapsulatedCommand.scaledMeterValue)
                    if (newValue > MAX_WATTS ) { return }											//Ignore values that are too high, definitely incorrect
					formattedValue = newValue as String
					if (formattedValue != state.W_L2) {
						state.W_L2 = formattedValue
						[name: "W_L2", value: formattedValue, unit: "", descriptionText: "L2 Power: ${formattedValue} W"]
					}
				} 
				else if (encapsulatedCommand.scale == 0 ){
					newValue = Math.round(encapsulatedCommand.scaledMeterValue * 100) / 100
					formattedValue = String.format("%5.1f", newValue)
					if (formattedValue != state.E_L2) {
						state.E_L2 = formattedValue
						[name: "E_L2", value: formattedValue, unit: "", descriptionText: "L2 Energy: ${formattedValue} kWh"]
					}
				} 
				else if (encapsulatedCommand.scale == 1 ){
					newValue = Math.round(encapsulatedCommand.scaledMeterValue * 100) / 100
					formattedValue = String.format("%5.1f", newValue)
					if (formattedValue != state.E_L2) {
						state.E_L2 = formattedValue
						[name: "E_L2", value: formattedValue, unit: "", descriptionText: "L2 Energy: ${formattedValue} kVAh"]
					}
				}
				else if (encapsulatedCommand.scale == 5 ){
               		newValue = Math.round(encapsulatedCommand.scaledMeterValue * 100) / 100
                    if (newValue > MAX_AMPS) { return }												//Ignore values that are too high, definitely incorrect
                    formattedValue = String.format("%5.1f", newValue)
					if (formattedValue != state.A_L2) {
						state.A_L2 = formattedValue
						[name: "A_L2", value: formattedValue, unit: "", descriptionText: "L2 Current: ${formattedValue} A"]
					}
				}
	    		else if (encapsulatedCommand.scale == 4 ){
               		newValue = Math.round(encapsulatedCommand.scaledMeterValue * 100) / 100
					formattedValue = String.format("%5.1f", newValue)
					if (formattedValue != state.V_L2) {
						state.V_L2 = formattedValue
						[name: "V_L2", value: formattedValue, unit: "", descriptionText: "L2 Voltage: ${formattedValue} V"]
					}
               	}               			
			}
		}
	}
}


def zwaveEvent(physicalgraph.zwave.Command cmd) {
	// Handles all Z-Wave commands we aren't interested in
    log.debug "Unhandled event ${cmd}"
	[:]
}

// Read new values and display them on screen. Do not reset anything.
def refresh() {
	log.debug "refresh()"
    
    newMeasurements()		// Read new values from meter
    updateDisplay()			// Send new values to display
}

/*
def poll() {
	log.debug "poll()"
	refresh()
}
*/

def updateDisplay() {
	log.debug "updateDisplay() - E_L1_L2: ${state.E_L1_L2}"
	
    sendEvent(name: "V_L1_L2", value: state.V_L1_L2, unit: "")    
    sendEvent(name: "V_L1", value: state.V_L1, unit: "")
    sendEvent(name: "V_L2", value: state.V_L2, unit: "")
   	
   	sendEvent(name: "W_L1_L2", value: state.W_L1_L2, unit: "")
    sendEvent(name: "W_L1", value: state.W_L1, unit: "")     
    sendEvent(name: "W_L2", value: state.W_L2, unit: "")

	sendEvent(name: "A_L1_L2", value: state.A_L1_L2, unit: "")
	sendEvent(name: "A_L1", value: state.A_L1, unit: "")    
	sendEvent(name: "A_L2", value: state.A_L2, unit: "")
    
	sendEvent(name: "E_L1_L2", value: state.E_L1_L2, unit: "")
	sendEvent(name: "E_L1", value: state.E_L1, unit: "")
	sendEvent(name: "E_L2", value: state.E_L2, unit: "")

    sendEvent(name: "resetDate", value: state.resetDate, unit: "")	

}

	// ************************************************************************
    // * Request new values from HEM without resetting the device
    // ************************************************************************

def newMeasurements() {
    
    def measurementType = 0
    /*
    if(settings.energyMeasurement == "kWh") {measurementType = 0}
    else
    {measurementType = 1}
	*/
      
	def cmd = delayBetween( [
        zwave.meterV2.meterGet(scale: measurementType).format(),	// 0 = Requests kWh values; 1 = Requests kVAh values
		zwave.meterV2.meterGet(scale: 2).format(),					// Requests W values
		zwave.meterV2.meterGet(scale: 4).format(),					// Requests V values
		zwave.meterV2.meterGet(scale: 5).format()					// Requests A values
        
	], 1000)
    cmd
    }

	// ************************************************************************
    // * Reset - This mostly resets values and what is shown on the display in case it seems stuck or there are other issues.
    // * It is not meant to reset the meter deleting the running counter, there is another method for that.
    // ************************************************************************

def reset() {
	log.debug "reset()"

    // Reset state attributes
    state.E_L1_L2 =	"0"
    state.E_L1 =	"0"
    state.E_L2 =	"0"
    state.W_L1_L2 =	"0"
    state.W_L1 =	"0"
    state.W_L2 =	"0"
    state.A_L1_L2 =	"0"
    state.A_L1 =	"0"
    state.A_L2 =	"0"
    state.V_L1_L2 =	"0"
    state.V_L1 =	"0"
    state.V_L2 =	"0"
	
    // Clear tiles
	sendEvent(name: "E_L1_L2",		value: "", unit: "")
    sendEvent(name: "E_L1",			value: "", unit: "")
    sendEvent(name: "E_L2",			value: "", unit: "")
    sendEvent(name: "W_L1_L2",		value: "", unit: "")
    sendEvent(name: "W_L1",			value: "", unit: "")
    sendEvent(name: "W_L2",			value: "", unit: "")
    sendEvent(name: "A_L1_L2",		value: "", unit: "")
    sendEvent(name: "A_L1",			value: "", unit: "")
    sendEvent(name: "A_L2",			value: "", unit: "")
    sendEvent(name: "V_L1_L2",		value: "", unit: "")
    sendEvent(name: "V_L1",			value: "", unit: "")
    sendEvent(name: "V_L2",			value: "", unit: "")
	sendEvent(name: "resetDate",	value: "", unit: "")
    
    configure()											// Send configuration parameters
    
    newMeasurements()									// Request new values
    
    updateDisplay()										// Send new values to display

}

// Reset the energy counter and the date tracking the last reset
def resetCtr() {

    def dateString = new Date().format("dd-MMM-yy", location.timeZone)
    //def timeString = new Date().format("HH:MM", location.timeZone)    // Leaving this here just in case I want to add time as well

	state.resetDate = dateString
    
    return [
    zwave.meterV2.meterReset().format()					// Reset all values, without return it was not actually executing... is it now skipping updateDisplay()?
    ]

    updateDisplay()
}


	// ************************************************************************
    // * Configure the Aeon HEM device with desired settings
    // ************************************************************************

def configure() {
	log.debug "configure()"
    
	Long rg1Delay = settings.reportGroup1 as Long
    Long rg2Delay = settings.reportGroup2 as Long
    Long rg3Delay = settings.reportGroup3 as Long
    
    if (rg1Delay == null) {		// Shouldn't have to do this, but there seem to be initialization errors
		rg1Delay = 120
	}

	if (rg2Delay == null) {
		rg2Delay = 60
	}
    
    if (rg3Delay == null) {
		rg3Delay = 10
	}
    
    
	def cmd = delayBetween([

	    /*
	    100 Set 101-103 to default. Default: N/A Size: 1
		101 Which reports need to send in Report group 1 (See flags in table below). Default: 0x00 00 00 02 Size: 4
		102 Which reports need to send in Report group 2 (See flags in table below). Default: 0x00 00 00 01 Size: 4
		103 Which reports need to send in Report group 3 (See flags in table below). Defualt: 0 Size: 4
		110 Set 111-113 to default. Default: N/A Size: 1
		111 The time interval of sending Report group 1 (Valid values 0x01-0xFFFFFFFF). Default: 0x00 00 00 05 Size: 4
		112 The time interval of sending Report group 2 (Valid values 0x01-0xFFFFFFFF). Default: 0x00 00 00 78 Size: 4
		113 The time interval of sending Report group 3 (Valid values 0x01-0xFFFFFFFF). Default: 0x00 00 00 78 Size: 4
		*/

		zwave.configurationV1.configurationSet(parameterNumber: 3, size: 1, scaledConfigurationValue: 0).format(),				// Disable (=0) selective reporting
		//zwave.configurationV1.configurationSet(parameterNumber: 4, size: 2, scaledConfigurationValue: 5).format(),			// Don't send whole HEM unless watts have changed by 30
		//zwave.configurationV1.configurationSet(parameterNumber: 5, size: 2, scaledConfigurationValue: 5).format(),			// Don't send L1 Data unless watts have changed by 15
		//zwave.configurationV1.configurationSet(parameterNumber: 6, size: 2, scaledConfigurationValue: 5).format(),			// Don't send L2 Data unless watts have changed by 15
		//zwave.configurationV1.configurationSet(parameterNumber: 8, size: 1, scaledConfigurationValue: 1).format(),			// Or by 5% (whole HEM)
		//zwave.configurationV1.configurationSet(parameterNumber: 9, size: 1, scaledConfigurationValue: 1).format(),			// Or by 5% (L1)
		//zwave.configurationV1.configurationSet(parameterNumber: 10, size: 1, scaledConfigurationValue: 1).format(),			// Or by 5% (L2)
        
		//zwave.configurationV1.configurationSet(parameterNumber: 100, size: 1, scaledConfigurationValue: 0).format(),			// reset to default 101 to 103
		zwave.configurationV1.configurationSet(parameterNumber: 101, size: 4, scaledConfigurationValue: 6145).format(),   		// Report Group 1: Whole HEM and L1/L2 power in kWh
		//zwave.configurationV1.configurationSet(parameterNumber: 101, size: 4, scaledConfigurationValue: 6149).format(),   	// Report Group 1: All L1/L2 kWh, total Volts & kWh
		zwave.configurationV1.configurationSet(parameterNumber: 102, size: 4, scaledConfigurationValue: 1573646).format(),  	// Report Group 2: L1/L2 for Amps & Watts, Whole HEM for Amps, Watts, & Volts
		//zwave.configurationV1.configurationSet(parameterNumber: 102, size: 4, scaledConfigurationValue: 1572872).format(),	// Report Group 2: Amps L1, L2, Total
        zwave.configurationV1.configurationSet(parameterNumber: 103, size: 4, scaledConfigurationValue: 770).format(),			// Report Group 3: Power (Watts) L1, L2, Total
		zwave.configurationV1.configurationSet(parameterNumber: 111, size: 4, scaledConfigurationValue: rg1Delay).format(),	 	// Send Report Group 1 every x seconds
		//zwave.configurationV1.configurationSet(parameterNumber: 111, size: 4, scaledConfigurationValue: 60).format(), 		// Send Report Group 1 every 60 seconds
		zwave.configurationV1.configurationSet(parameterNumber: 112, size: 4, scaledConfigurationValue: rg2Delay).format(),		// Send Report Group 2 every x seconds
		//zwave.configurationV1.configurationSet(parameterNumber: 112, size: 4, scaledConfigurationValue: 30).format(), 		// Send Report Group 2 every 30 seconds
		zwave.configurationV1.configurationSet(parameterNumber: 113, size: 4, scaledConfigurationValue: rg3Delay).format() 		// Send Report Group 3 every x seconds
        //zwave.configurationV1.configurationSet(parameterNumber: 113, size: 4, scaledConfigurationValue: 6).format() 			// Send Report Group 3 every 6 seconds
	], 2000)																													// 2000ms delay between commands
	log.debug cmd

	cmd
}