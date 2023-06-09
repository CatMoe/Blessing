package catmoe.fallencrystal.moefilter.api.command.annotation

@Retention(AnnotationRetention.RUNTIME)
@Target(AnnotationTarget.CLASS)
annotation class Command(val command: String)
