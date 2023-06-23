package catmoe.fallencrystal.moefilter.common.config

import com.typesafe.config.Config
import com.typesafe.config.ConfigFactory

object ObjectConfig {
    private var config = ConfigFactory.parseFile(LoadConfig.getConfigFile())
    private var message = ConfigFactory.parseFile(LoadConfig.getMessage())
    private var proxy = ConfigFactory.parseFile(LoadConfig.getProxy())
    private var antibot: Config = ConfigFactory.parseFile(LoadConfig.getAntibot())

    fun getConfig(): Config { return config }

    fun getMessage(): Config { return message }

    fun getProxy(): Config { return proxy }

    fun getAntibot(): Config { return antibot }

    fun reloadConfig() {
        config = ConfigFactory.parseFile(LoadConfig.getConfigFile())
        message = ConfigFactory.parseFile(LoadConfig.getMessage())
        proxy = ConfigFactory.parseFile(LoadConfig.getProxy())
        antibot = ConfigFactory.parseFile(LoadConfig.getAntibot())
    }
}