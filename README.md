# Aeon-HEMv2-Gen2-SmartThings-Device-Handler

*Aeon Home Energy Meter v2 Gen2 Basic Edition - Version: 0.9b*

*Disclaimer*: This WILL NOT work with Aeon's HEM Gen1 or Gen5 (latest version) as is intended to be used for HEMs installed on the typical 200A 240V split-phase US residential systems (Two 120V legs and a neutral - grounded center tap from power transformer outside your house).
 
*Note*:	This device handler is based off of Smartthings' "Aeon Smart Meter" device handler sample code but mostly on a device handler written by Barry A. Burke (Thank you!!). I have made very significant changes to his code, stripped stuff, and added other stuff but the overall structure likely remains (for now at least).
 
*Goal*:	I removed all support for v1 (sorry!) to keep the code simple and pertinent to what I thought was the latest version of the HEM - v2, but I just realized Aeon released Gen5 with zwave plus. I wanted to have a device handler that fully supported Android as I would never even dream of buying any Apple products! I also wanted a way to keep track of my usage to get the best deal out of my energy provier's contract. Calculating cost was of no use to me given the price per kWh changes based on my consumption. In my case, if I am between 1000kWh and 2000kWh in a sinlgle month, I get a $100 credit on my account so this device handler just has a counter you can reset monthly. Last, I removed one of the digits after the decimal as it is generally superfluos for typical HEM applications and it is also likely that the accuracy of the HEM doesn't guarantee the accuracy of the measurement anyway (at least not over time given there is no calibration feature).
 
 *To Do*:
 - Possibly add back some of the min/max functionality but I am not sure how useful that would be
 - Add preference to switch from kWh to kVAh
 - Enable Debug on/off preference to declutter logs when debugging is not needed
 - Once debug on/off is enabled, add more debugging to help troubleshoot issues
 - Leg 1 and Leg 2 voltage measurement is not being read. Figure out configuration settings to make it work even though I am not displaying the values
 - Check whether polling is needed, and enable if needed
 - Refresh and Configure button may not be necessary, evaluate and leave/remove as needed
 - Figure out whether I am making the best use of the capability defined attributes, or can I just ignore them?
 - Figure out Reporting Group 1, 2, and 3 - what is sent and how often. Goal: reduce network congestion
 - Report delays may require delay values in Hexadecimal so passing 120s might require entering 78. Reports seem to run too frequently
 - Figure out why at times the values in the tile are pushed down... ST bug or programming issue?
 - Why is tile text not resizing?
 - Tile color should go from green to yellow to red, rather than blue to red as values become to high or too low (only V).
 - Review foregroundColor and backgroundColor as it does not seem to be doing anything on some tiles. Fix or remove as needed.
 - My voltage measurement always seems a bit high, validate and add configurable +- offset to measured value
 - Verify W/A measurements using clamp meter on main panel. Add configurable +- offset to measured values
 
 
 *History*:
  
 2016-07-15:	- Basic functionality seems to work but lots more work is necessary
 
 *Disclaimer 2*:	I am NOT a developer. I learn as I go so please do NOT rely on this for anything critical. Use it and change it as needed but not for commercial purposes. I will not make any changes to this code that fix things on iOS if it breaks anything on Android - sorry! Also, I barely got to this point so adding new features may be out of my reach for now.
