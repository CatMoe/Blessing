package catmoe.fallencrystal.moefilter.api.command.annotation

import catmoe.fallencrystal.moefilter.api.command.annotation.misc.DescriptionFrom

@Retention(AnnotationRetention.RUNTIME)
@Target(AnnotationTarget.CLASS)
annotation class CommandDescription(val type: DescriptionFrom, val description: String)
