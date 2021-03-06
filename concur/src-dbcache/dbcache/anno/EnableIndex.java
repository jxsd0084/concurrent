package dbcache.anno;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * 启用实体索引缓存服务
 * 可使用dbcache.anno.Cached#enableIndex()代替
 * @author Jake
 * @date 2014年9月14日下午8:38:07
 */
@Target(ElementType.FIELD)
@Retention(RetentionPolicy.RUNTIME)
public @interface EnableIndex {

}
