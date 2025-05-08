package de.heinzenburger.g2_weckmichmal.core

import de.heinzenburger.g2_weckmichmal.persistence.Logger
import de.heinzenburger.g2_weckmichmal.specifications.ConfigurationWithEvent
import de.heinzenburger.g2_weckmichmal.specifications.Configuration
import de.heinzenburger.g2_weckmichmal.specifications.Event
import de.heinzenburger.g2_weckmichmal.specifications.I_Core

//Is a mockup version of real core, in order to keep UI preview functionality
//Most methods do nothing, only those that cause something in UI are relevant
//For description of each method, see I_Core in specifications
class MockupCore : I_Core {
    override fun deriveStationName(input: String): List<String> {
        return listOf("Europapapapa")
    }

    companion object{
        val mockupConfigurations = listOf(Configuration.emptyConfiguration)
        val mockupEvents = listOf(Event.emptyEvent)
    }

    override fun runUpdateLogic() {}
    override fun runWakeUpLogic() {}
    override fun startUpdateScheduler() {}
    override fun saveRaplaURL(url : String){}
    override fun isApplicationOpenedFirstTime(): Boolean? {return null}
    override fun generateOrUpdateAlarmConfiguration(configuration: Configuration) {}

    override fun getRaplaURL(): String? {
        return ""
    }

    override fun log(
        level: Logger.Level,
        text: String
    ) {
    }

    override fun getLog(): String {
        return "Lorem ipsum dolor sit amet, consetetur sadipscing elitr, sed diam nonumy eirmod tempor invidunt ut labore et dolore magna aliquyam erat, sed diam voluptua. At vero eos et accusam et justo duo dolores et ea rebum. Stet clita kasd gubergren, no sea takimata sanctus est Lorem ipsum dolor sit amet. Lorem ipsum dolor sit amet, consetetur sadipscing elitr, sed diam nonumy eirmod tempor invidunt ut labore et dolore magna aliquyam erat, sed diam voluptua. At vero eos et accusam et justo duo dolores et ea rebum. Stet clita kasd gubergren, no sea takimata sanctus est Lorem ipsum dolor sit amet. Lorem ipsum dolor sit amet, consetetur sadipscing elitr, sed diam nonumy eirmod tempor invidunt ut labore et dolore magna aliquyam erat, sed diam voluptua. At vero eos et accusam et justo duo dolores et ea rebum. Stet clita kasd gubergren, no sea takimata sanctus est Lorem ipsum dolor sit amet.  \n" +
                "\n" +
                "Duis autem vel eum iriure dolor in hendrerit in vulputate velit esse molestie consequat, vel illum dolore eu feugiat nulla facilisis at vero eros et accumsan et iusto odio dignissim qui blandit praesent luptatum zzril delenit augue duis dolore te feugait nulla facilisi. Lorem ipsum dolor sit amet, consectetuer adipiscing elit, sed diam nonummy nibh euismod tincidunt ut laoreet dolore magna aliquam erat volutpat.  \n" +
                "\n" +
                "Ut wisi enim ad minim veniam, quis nostrud exerci tation ullamcorper suscipit lobortis nisl ut aliquip ex ea commodo consequat. Duis autem vel eum iriure dolor in hendrerit in vulputate velit esse molestie consequat, vel illum dolore eu feugiat nulla facilisis at vero eros et accumsan et iusto odio dignissim qui blandit praesent luptatum zzril delenit augue duis dolore te feugait nulla facilisi.  \n" +
                "\n" +
                "Nam liber tempor cum soluta nobis eleifend option congue nihil imperdiet doming id quod mazim placerat facer possim assum. Lorem"
    }

    override fun updateConfigurationActive(
        isActive: Boolean,
        configuration: Configuration
    ) {

    }

    override fun getAllConfigurationAndEvent(): List<ConfigurationWithEvent>? {
        var result = mutableListOf<ConfigurationWithEvent>()
        mockupConfigurations.forEach {
            val configurationEntity = it
            var event : Event? = null
            mockupEvents.forEach { if(it.configID == configurationEntity.uid){ event = it } }
            result.add(ConfigurationWithEvent(configurationEntity,event))
        }
        return result
    }

    override fun isValidCourseURL(urlString : String) : Boolean {
        return true
    }


    override fun validateConfigurationEntity(configuration: Configuration): Boolean {
        return true
    }

    override fun deleteAlarmConfiguration(uid: Long) {
    }

    override fun showToast(message: String) {
    }
}