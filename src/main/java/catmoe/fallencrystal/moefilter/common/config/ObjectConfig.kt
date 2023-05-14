package catmoe.fallencrystal.moefilter.common.config

import com.typesafe.config.Config
import com.typesafe.config.ConfigFactory

object ObjectConfig {
    val config = ConfigFactory.parseFile(LoadConfig.getConfigFile())
    val message = ConfigFactory.parseFile(LoadConfig.getMessage())

    fun getConfig(): Config { return config!! }

    fun getMessage(): Config { return message!! }
}