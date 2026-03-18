import SwiftUI
import GoogleHomeSDK
import GoogleHomeTypes

@main
struct iOSApp: App {
    
    init() {
        Home.configure {
            $0.sharedAppGroup = "group.nordicsemi.nrf.matter"
            $0.referencedAutomationTypes = ReferencedAutomationTypes(
                deviceTypes: [
                    OnOffLightDeviceType.self,
                    TemperatureSensorDeviceType.self,
                    WindowCoveringDeviceType.self,
                    GoogleTVDeviceType.self,
                    GoogleCameraDeviceType.self,
                    GoogleDoorbellDeviceType.self,
                ],
                traits: [
                    Google.TimeTrait.self,
                    Google.TogglesTrait.self,
                    Google.VolumeTrait.self,
                    Google.ExtendedMediaInputTrait.self,
                    Google.SimplifiedOnOffTrait.self,
                    Google.BrightnessTrait.self,
                    Google.ExtendedFanControlTrait.self,
                    Google.ExtendedThermostatTrait.self,
                    Google.SimplifiedThermostatTrait.self,
                    Google.AreaPresenceStateTrait.self,
                    Google.AreaAttendanceStateTrait.self,
                    Google.WebRtcLiveViewTrait.self,
                    Google.PushAvStreamTransportTrait.self,
                    Matter.LevelControlTrait.self,
                    Matter.OnOffTrait.self,
                    Matter.TemperatureMeasurementTrait.self,
                    Matter.WindowCoveringTrait.self,
                    Matter.MediaPlaybackTrait.self,
                    Matter.TotalVolatileOrganicCompoundsConcentrationMeasurementTrait.self,
                    Matter.AirQualityTrait.self,
                    Matter.CarbonDioxideConcentrationMeasurementTrait.self,
                    Matter.CarbonMonoxideConcentrationMeasurementTrait.self,
                    Matter.OperationalStateTrait.self,
                    Matter.ColorControlTrait.self,
                    Matter.ThermostatTrait.self,
                    Matter.TemperatureControlTrait.self,
                    Matter.RelativeHumidityMeasurementTrait.self,
                    Matter.Pm25ConcentrationMeasurementTrait.self,
                    Matter.OvenCavityOperationalStateTrait.self,
                    Matter.RvcOperationalStateTrait.self,
                ]
            )
        }
    }
    
    var body: some Scene {
        WindowGroup {
            ContentView()
                .ignoresSafeArea()
        }
    }
}
