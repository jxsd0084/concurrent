package transfer.serializer;

import org.objectweb.asm.MethodVisitor;
import transfer.Outputable;
import transfer.compile.AsmSerializerContext;
import transfer.core.ClassInfo;
import transfer.core.FieldInfo;
import transfer.def.PersistConfig;
import transfer.def.Types;
import transfer.utils.BitUtils;
import transfer.utils.IdentityHashMap;
import transfer.utils.TypeUtils;

import java.lang.reflect.Type;

/**
 * 带标签的对象编码器 Created by Jake on 2015/2/23.
 */
public class TagObjectSerializer implements Serializer {

	// 标签编码器
	private static final ShortStringSerializer STRING_SERIALIZER = ShortStringSerializer
			.getInstance();

	@Override
	public void serialze(Outputable outputable, Object object,
			IdentityHashMap referenceMap) {

		if (object == null) {
			NULL_SERIALIZER.serialze(outputable, null, referenceMap);
			return;
		}

		Class<?> clazz = object.getClass();

		ClassInfo classInfo = PersistConfig.getOrCreateClassInfo(clazz);

		outputable.putByte(Types.OBJECT);

		// 添加类Id
		BitUtils.putInt2(outputable, classInfo.getClassId());

		// 添加属性个数
		BitUtils.putInt2(outputable, classInfo.getFieldInfos().size());

		for (FieldInfo fieldInfo : classInfo.getFieldInfos()) {

			// 添加属性标签
			STRING_SERIALIZER.serialze(outputable, fieldInfo.getFieldName(),
					referenceMap);

			// 序列化属性值
			Serializer fieldSerializer = PersistConfig.getSerializer(TypeUtils
					.getRawClass(fieldInfo.getType()));

			Object fieldValue = fieldInfo.getField(object);

			fieldSerializer.serialze(outputable, fieldValue, referenceMap);

		}

	}

	@Override
	public void compile(Type type, MethodVisitor mw,
			AsmSerializerContext context) {

	}

	private static TagObjectSerializer instance = new TagObjectSerializer();

	public static TagObjectSerializer getInstance() {
		return instance;
	}
}
