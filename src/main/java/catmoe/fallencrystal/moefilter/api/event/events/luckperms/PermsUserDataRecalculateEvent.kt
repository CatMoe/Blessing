package catmoe.fallencrystal.moefilter.api.event.events.luckperms

import net.luckperms.api.LuckPerms
import net.luckperms.api.cacheddata.CachedDataManager
import net.luckperms.api.event.LuckPermsEvent
import net.luckperms.api.model.user.User

/*
https://github.com/LuckPerms/LuckPerms/blob/master/api/src/main/java/net/luckperms/api/event/user/UserDataRecalculateEvent.java
使用或借鉴其代码前请先遵守许可证
 */
class PermsUserDataRecalculateEvent(val user: User, val data: CachedDataManager, val luckperms: LuckPerms, val event: Class<out LuckPermsEvent>)