package transfer;

import transfer.core.ByteMeta;
import transfer.def.PersistConfig;
import transfer.def.Types;
import transfer.deserializer.CollectionDeSerializer;
import transfer.deserializer.Deserializer;
import transfer.deserializer.EntryDeserializer;
import transfer.deserializer.MapDeSerializer;
import transfer.serializer.Serializer;
import transfer.utils.IdentityHashMap;
import transfer.utils.IntegerMap;
import transfer.utils.TypeUtils;

import java.lang.reflect.Type;
import java.util.Collection;
import java.util.Iterator;
import java.util.Map;

/**
 * 字节存储协议
 * <br/>兼容新旧版本的类
 * <br/>定义存储类必须调用Config#registerClass(java.lang.Class<?>, int)进行注册, 或使用注解@Transferable
 * Created by Jake on 2015/2/22.
 */
public class Persister {


    // notes:
    // 可迭代解码,调用Iterator.next()方法时进行集合元素的解码
    // Inputable、Outputable适配网络框架的readBuffer、writeBuffer
    // TypeReference指定泛型类型将预编译,可提升解析速度
    //


	 /**
     * 编码
     * @param object 目标对象
     */
    public static ByteArray encode(Object object) {
        return encode(object, 128);
    }


    /**
     * 编码
     * @param object 目标对象
     * @param bytesLength 编码字节长度(估算)
     */
    public static ByteArray encode(Object object, int bytesLength) {
        if (object == null) {
            ByteBuffer buffer = new ByteBuffer(1);
            Serializer.NULL_SERIALIZER.serialze(buffer, null, null);
            return buffer.getByteArray();
        }

        ByteBuffer buffer = new ByteBuffer(bytesLength);
        encode(buffer, object);
        return buffer.getByteArray();
    }


    /**
     * 编码
     * @param object 目标对象
     * @param type 指定预编译目标对象的类型
     * @See transfer.Transfer.encodePreCompile(Type)
     */
    public static ByteArray encode(Object object, Type type) {
        return encode(object, type, 128);
    }


    /**
     * 编码
     * @param object 目标对象
     * @param type 指定预编译目标对象的类型
     * @param bytesLength 编码字节长度(估算)
     * @See transfer.Transfer.encodePreCompile(Type)
     */
    public static ByteArray encode(Object object, Type type, int bytesLength) {
        if (object == null) {
            ByteBuffer buffer = new ByteBuffer(1);
            Serializer.NULL_SERIALIZER.serialze(buffer, null, null);
            return buffer.getByteArray();
        }

        ByteBuffer buffer = new ByteBuffer(bytesLength);
        encode(buffer, object, type);
        return buffer.getByteArray();
    }

    
    /**
     * 编码
     * @param outputable 输出接口
     * @param object 目标对象
     */
    public static void encode(Outputable outputable, Object object) {
        if (object == null) {
            Serializer.NULL_SERIALIZER.serialze(outputable, null, null);
            return;
        }
        
        Serializer serializer = PersistConfig.getSerializer(object.getClass());
        serializer.serialze(outputable, object, new IdentityHashMap(16));
    }
    
    
    /**
     * 编码
     * @param outputable 输出接口
     * @param object 目标对象
     * @param type 指定预编译目标对象的类型
     */
    public static <T> void encode(Outputable outputable, T object, Type type) {
        if (object == null) {
            Serializer.NULL_SERIALIZER.serialze(outputable, null, null);
            return;
        }

        Serializer serializer = PersistConfig.getCompiledSerializer(type);
        if (serializer == null) {
            serializer = PersistConfig.preCompileSerializer(type); // 进行预编译
        }
        serializer.serialze(outputable, object, new IdentityHashMap(16));
    }
    
    
    /**
     * 编码
     * @param outputable 输出接口
     * @param object 目标对象
     * @param typeReference 类型定义
     */
    public static <T> void encode(Outputable outputable, T object, TypeReference<T> typeReference) {
        if (object == null) {
            Serializer.NULL_SERIALIZER.serialze(outputable, null, null);
            return;
        }

        Type type = typeReference.getType();
        encode(outputable, object, type);
    }
    
    
    /**
     * 编码器预编译
     * <br/>调用此方法可预编译或者Transfer#encode(Object, Type)指定预编译类型
     * @param type
     * @return
     * @see transfer.Transfer.encode(Object, Type)
     */
    public static Serializer encodePreCompile(Type type) {
        return PersistConfig.preCompileSerializer(type);
    }
    
    /**
     * 解码
     * @param inputable 输入接口
     * @param <T>
     * @return
     */
    public static <T> T decode(Inputable inputable) {
        byte flag = inputable.getByte();
        Deserializer deserializer = PersistConfig.getDeserializer((Type) Object.class, flag);
        return deserializer.deserialze(inputable, Object.class, flag, new IntegerMap(16));
    }
    
    
    /**
     * 解码
     * @param inputable 输入接口
     * @param clazz 类型
     * @param <T>
     * @return
     */
    public static <T> T decode(Inputable inputable, Class<T> clazz) {
        Deserializer deserializer = PersistConfig.getCompiledDeSerializer(clazz);
        if (deserializer == null) {
        	deserializer = PersistConfig.preCompileDeserializer(clazz); // 进行预编译
        }
        
        byte flag = inputable.getByte();
        return deserializer.deserialze(inputable, clazz, flag, new IntegerMap(16));
    }


    /**
     * 解码
     * @param bytes 输入字节数组
     * @param clazz 类 型
     * @param <T>
     * @return
     */
    public static <T> T decode(byte[] bytes, Class<T> clazz) {
        return decode(new ByteArray(bytes), clazz);
    }


    /**
     * 解码
     * @param inputable 输入接口
     * @param typeReference 类型定义
     * @param <T>
     * @return
     */
    public static <T> T decode(Inputable inputable, TypeReference<T> typeReference) {
    	Deserializer deserializer = PersistConfig.getCompiledDeSerializer(typeReference.getType());
        if (deserializer == null) {
        	deserializer = PersistConfig.preCompileDeserializer(typeReference.getType()); // 进行预编译
        }
    	
        byte flag = inputable.getByte();
        return deserializer.deserialze(inputable, typeReference.getType(), flag, new IntegerMap(16));
    }



    /**
     * 解码
     * @param bytes 输入字节数组
     * @param typeReference 类型定义
     * @param <T>
     * @return
     */
    public static <T> T decode(byte[] bytes, TypeReference<T> typeReference) {
        return decode(new ByteArray(bytes), typeReference);
    }



    /**
     * 迭代解码
     * @param inputable 输入字节数组
     * @param typeReference 类型定义
     * @param <T> 集合类型类型
     * @return
     */
    public static <T extends Collection<E>, E> Iterator<E> iterator(final Inputable inputable, TypeReference<T> typeReference) {

        // 读取消息头
        final ByteMeta byteDataMeta = CollectionDeSerializer.getInstance().readMeta(inputable);
        // 不可以迭代
        if (byteDataMeta == null || !byteDataMeta.isIteratorAble()) {
            throw new UnsupportedOperationException();
        }

        
        final Deserializer defaultComponentDeserializer;
        final Type componentType = TypeUtils.getParameterizedClass(typeReference.getType(), 0);// 取出元素类型
        
        if (componentType != null && componentType != Object.class) {
            defaultComponentDeserializer = PersistConfig.getDeserializer(componentType);// 元素解析器
        } else {
            defaultComponentDeserializer = null;
        }


        final IntegerMap referenceMap = new IntegerMap(16);
        return new Iterator<E>() {
            private int curIndex = 0;
            private int size = byteDataMeta.getComponentSize();

            @Override
            public boolean hasNext() {
                return curIndex < size;
            }

            @Override
            public E next() {
                curIndex ++;
                if (defaultComponentDeserializer == null) {
                    final byte elementFlag = inputable.getByte();
                    final Deserializer componentDeserializer = PersistConfig.getDeserializer(componentType, elementFlag);// 元素解析器
                    return componentDeserializer.deserialze(inputable, componentType, elementFlag, referenceMap);
                } else {
                    return defaultComponentDeserializer.deserialze(inputable, componentType, inputable.getByte(), referenceMap);
                }
            }

            @Override
            public void remove() {
                throw new UnsupportedOperationException();
            }
        };
    }


    /**
     * 迭代解码
     * @param bytes 输入字节数组
     * @param typeReference 类型定义
     * @param <T> 集合类型类型
     * @return
     */
    public static <T extends Collection<E>, E> Iterator<E> iterator(byte[] bytes, TypeReference<T> typeReference) {
        return iterator(new ByteArray(bytes), typeReference);
    }


    /**
     * 迭代解码
     * @param inputable 输入接口
     * @param typeReference 类型定义
     * @param <T> Map类型
     * @return
     */
    public static <T extends Map<K, V>, K, V> Iterator<Map.Entry<K, V>> iteratorMap(final Inputable inputable, TypeReference<T> typeReference) {
        // 读取消息头
        final ByteMeta byteDataMeta = MapDeSerializer.getInstance().readMeta(inputable);
        // 不可以迭代
        if (byteDataMeta == null || !byteDataMeta.isIteratorAble()) {
            throw new UnsupportedOperationException();
        }

        final Type componentType = typeReference.getType();// 取出元素类型
        final Deserializer entryDeserializer = EntryDeserializer.getInstance();// 元素解析器

        final IntegerMap referenceMap = new IntegerMap(16);
        return new Iterator<Map.Entry<K, V>>() {
            private int curIndex = 0;
            private int size = byteDataMeta.getComponentSize();

            @Override
            public boolean hasNext() {
                return curIndex < size;
            }

            @Override
            public Map.Entry<K, V> next() {
                curIndex ++;
                return entryDeserializer.deserialze(inputable, componentType, Types.UNKOWN, referenceMap);
            }

            @Override
            public void remove() {
                throw new UnsupportedOperationException();
            }
        };
    }


    /**
     * 迭代解码
     * @param bytes 输入字节数组
     * @param typeReference 类型定义
     * @param <T> Map类型
     * @return
     */
    public static <T extends Map<K, V>, K, V> Iterator<Map.Entry<K, V>> iteratorMap(byte[] bytes, TypeReference<T> typeReference) {
        return iteratorMap(new ByteArray(bytes), typeReference);
    }


}
