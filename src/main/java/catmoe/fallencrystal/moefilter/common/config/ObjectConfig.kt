package catmoe.fallencrystal.moefilter.common.config

import com.typesafe.config.Config
import com.typesafe.config.ConfigFactory

object ObjectConfig {
    private val config: Config = ConfigFactory.parseFile(LoadConfig.getConfigFile())
    private val message: Config = ConfigFactory.parseFile(LoadConfig.getMessage())

    fun getConfig(): Config { return config!! }

    fun getMessage(): Config { return message!! }
}