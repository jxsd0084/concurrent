package basesource.anno.check;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * 非空字符串
 * @author Jake
 *
 */
@Target({ElementType.FIELD, ElementType.PARAMETER})
@Retention(RetentionPolicy.RUNTIME)
public @interface NotBlank {
	
	public String msg() default "";
	
	/**
	 * 值(json格式的值使用:value.xxx.xx)
	 * @return
	 */
	public String value() default "value";
	
}
