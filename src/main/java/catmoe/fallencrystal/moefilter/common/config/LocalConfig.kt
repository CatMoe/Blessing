package catmoe.fallencrystal.moefilter.common.config

import com.typesafe.config.Config
import com.typesafe.config.ConfigFactory

object LocalConfig {
    private var config = ConfigFactory.parseFile(LoadConfig.instance.getConfigFile())
    private var message = ConfigFactory.parseFile(LoadConfig.instance.getMessage())
    private var proxy = ConfigFactory.parseFile(LoadConfig.instance.getProxy())
    private var antibot: Config = ConfigFactory.parseFile(LoadConfig.instance.getAntibot())

    fun getConfig(): Config { return config }

    fun getMessage(): Config { return message }

    fun getProxy(): Config { return proxy }

    fun getAntibot(): Config { return antibot }

    fun reloadConfig() {
        config = ConfigFactory.parseFile(LoadConfig.instance.getConfigFile())
        message = ConfigFactory.parseFile(LoadConfig.instance.getMessage())
        proxy = ConfigFactory.parseFile(LoadConfig.instance.getProxy())
        antibot = ConfigFactory.parseFile(LoadConfig.instance.getAntibot())
    }
}