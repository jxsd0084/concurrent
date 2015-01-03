package dbcache.model;

import java.io.Serializable;

/**
 * 索引值对象
 * @author Jake
 * @date 2014年8月25日上午1:07:14
 */
public class IndexValue<PK extends Comparable<PK> & Serializable> extends IndexKey {

	/**
	 * 索引值对应的实体Id
	 */
	private final PK id;

	/**
	 * 构造方法
	 * @param name 索引名
	 * @param value 索引值
	 * @param id 实体Id
	 */
	public IndexValue(String name, Object value, PK id) {
		super(name, value);
		this.id = id;
	}

	/**
	 * 获取实例
	 * @param name 索引名
	 * @param value 索引值
	 * @param id 实体Id
	 * @return
	 */
	public static <PK extends Comparable<PK> & Serializable> IndexValue<PK> valueOf(String name, Object value, PK id) {
		return new IndexValue<PK>(name, value, id);
	}

	public PK getId() {
		return id;
	}


}
