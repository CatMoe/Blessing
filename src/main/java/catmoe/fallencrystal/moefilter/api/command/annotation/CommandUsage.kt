package catmoe.fallencrystal.moefilter.api.command.annotation

@Retention(AnnotationRetention.RUNTIME)
@Target(AnnotationTarget.CLASS)
annotation class CommandUsage(val usage: Array<String>)
