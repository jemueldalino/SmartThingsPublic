/**
 *  NapinpodAPI
 *
 *  Copyright 2016 Jemuel Dalino
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
 */
definition(
    name: "OutletAPI",
    namespace: "OutletAPI",
    author: "Jemuel Dalino",
    description: "Control smart outletAPI",
    category: "Convenience",
    iconUrl: "https://s3.amazonaws.com/smartapp-icons/Convenience/Cat-Convenience.png",
    iconX2Url: "https://s3.amazonaws.com/smartapp-icons/Convenience/Cat-Convenience@2x.png",
    iconX3Url: "https://s3.amazonaws.com/smartapp-icons/Convenience/Cat-Convenience@2x.png",
    oauth: true)


preferences {
	section ("Allow external service to control these things...") {
    input "switches", "capability.switch", multiple: true, required: true
  }
}

mappings {
  path("/switches") {
    action: [
      GET: "listSwitches"
    ]
  }
  path("/switches/:command") {
    action: [
      POST: "updateSwitches"
    ]
  }
  path("/hubstatus"){
  	action: [
    	GET: "hubStatus"
    ]
  }
}

def installed() {
	log.debug "Installed with settings: ${settings}"

	initialize()
}

def updated() {
	log.debug "Updated with settings: ${settings}"

	unsubscribe()
	initialize()
}

def initialize() {
	// TODO: subscribe to attributes, devices, locations, etc.
}

// TODO: implement event handlers
// returns hub status
def hubStatus() {
	def resp = []
    switches.each {
      resp << [name: it.displayName, hubip: it.hub.localIP, value: it.latestValue("switch")]
    }
    return resp
}


// returns a list like
// [[name: "kitchen lamp", value: "off"], [name: "bathroom", value: "on"]]
def listSwitches() {
    def resp = []
    switches.each {
      resp << [Id: appSettings.PodId, DeviceTypeCode: "Outlets", DeviceStatusCode: it.currentValue("switch")]
    }
    return resp
}

def updateSwitches() {
    // use the built-in request object to get the command parameter
    def command = params.command
    def resp = []

    // all switches have the command
    // execute the command on all switches
    // (note we can do this on the array - the command will be invoked on every element
    switch(command) {
        case "on":
            switches.on()
            switches.each {
              resp << [Id: appSettings.PodId, DeviceTypeCode: "Outlets", DeviceStatusCode: it.currentValue("switch")]
            }
            return resp
            break
        case "off":
            switches.off()
            switches.each {
              resp << [Id: appSettings.PodId, DeviceTypeCode: "Outlets", DeviceStatusCode: it.currentValue("switch")]
            }
            break
        default:
            httpError(400, "$command is not a valid command for all switches specified")
    }
    log.debug resp
    return resp
}