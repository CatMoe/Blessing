package catmoe.fallencrystal.moefilter.common.config

import com.typesafe.config.Config
import com.typesafe.config.ConfigFactory

object ObjectConfig {
    private var config: Config = ConfigFactory.parseFile(LoadConfig.getConfigFile())
    private var message: Config = ConfigFactory.parseFile(LoadConfig.getMessage())

    fun getConfig(): Config { return config }

    fun getMessage(): Config { return message }

    fun reloadConfig() {
        config = ConfigFactory.parseFile(LoadConfig.getConfigFile())
        message = ConfigFactory.parseFile(LoadConfig.getMessage())
    }
}