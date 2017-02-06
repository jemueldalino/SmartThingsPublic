include 'asynchttp_v1'

/**
 *  DoorSensorAPI
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
    name: "DoorSensorAPI",
    namespace: "DoorSensorAPI",
    author: "Jemuel Dalino",
    description: "Control smart doorSensorAPI",
    category: "Safety & Security",
    iconUrl: "https://s3.amazonaws.com/smartapp-icons/Convenience/Cat-Convenience.png",
    iconX2Url: "https://s3.amazonaws.com/smartapp-icons/Convenience/Cat-Convenience@2x.png",
    iconX3Url: "https://s3.amazonaws.com/smartapp-icons/Convenience/Cat-Convenience@2x.png",
    oauth: true)
    {
    
    appSetting "PodId"
}


preferences {
	section ("Allow external service to control these things...") {
    input "switches", "capability.contactSensor", multiple: true, required: true
  }
}

mappings {
  path("/switches") {
    action: [
      GET: "listSensors"
    ]
  }
  
  path("/hublocalip"){
  	action: [
   	  GET: "hubLocalIp"
    ]
  }
  
  path("/open"){
  	action: [
   	  GET: "open"
    ]
  }
  
  path("/close"){
  	action: [
   	  GET: "close"
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
	// subscribe to attributes, devices, locations, etc.
    subscribe(switches, "contact.open", contactSensorOpenHandler)
    subscribe(switches, "contact.closed", contactSensorClosedHandler)
}

// implement event handlers
def contactSensorOpenHandler(evt) {
    open()
}

def contactSensorClosedHandler(evt) {
    close()
    lockDoor()
}

// returns hub status
def hubLocalIp() {
    return [localip: switches[0].hub.localIP]
}

def open() {
	log.debug "sensor open!"
    
    def params = [
        uri: 'http://api.napinpod.com',
        path: '/api/podstatuses/editpoddevicestatus',
        body: [Id: appSettings.PodId, DeviceTypeCode: "Sensors", DeviceStatusCode: "Open"]
    ]
    asynchttp_v1.post(processResponse, params)
}

def close() {
	log.debug "sensor closed!"
    
    def params = [
        uri: 'http://api.napinpod.com',
        path: '/api/podstatuses/editpoddevicestatus',
        body: [Id: appSettings.PodId, DeviceTypeCode: "Sensors", DeviceStatusCode: "Closed"]
    ]
    asynchttp_v1.post(processResponse, params)
}

def lockDoor() {
    log.debug "send signal to lock door!"
    
    try{
    	httpPost("http://api.napinpod.com/api/pods/lock?id="+appSettings.PodId, "") {response ->
            def status = response.status

                switch (status) {
                    case 200:
                        log.debug "200 returned"
                        locked()
                        break
                    case 304:
                        log.debug "304 returned"
                        break
                    default:
                        log.warn "no handling for response with status $status"
                        break
                }
        }
    } catch(e){
    	log.error "something went wrong: $e"
    }
}

def locked() {
	log.debug "send signal to update lock status"
    
    def params = [
        uri: 'http://api.napinpod.com',
        path: '/api/podstatuses/editpoddevicestatus',
        body: [Id: appSettings.PodId, DeviceTypeCode: "Locks", DeviceStatusCode: "Locked"]
    ]
    asynchttp_v1.post(processResponse, params)
}

// returns a list like
// [[name: "kitchen lamp", value: "open"], [name: "bathroom", value: "closed"]]
def listSensors() {
    def resp = []
    switches.each {
      resp << [Id: appSettings.PodId, DeviceTypeCode: "Sensors", DeviceStatusCode: it.currentValue("contact")]
    }
    return resp
}

def processResponse(response, data) {
    if (response.hasError()) {
        log.error "response has error: $response.errorMessage"
    } else {
        def status = response.status
        
        switch (status) {
            case 200:
                log.debug "200 returned"
                break
            case 304:
                log.debug "304 returned"
                break
            default:
                log.warn "no handling for response with status $status"
                break
        }
    }
}