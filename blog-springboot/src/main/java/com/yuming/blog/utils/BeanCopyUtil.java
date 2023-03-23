package com.yuming.blog.utils;



import java.util.ArrayList;
import java.util.List;

/**
 * 复制对象或集合属性
 *
 */

public class BeanCopyUtil {

    /**
     * 根据现有对象的属性创建目标对象，并赋值
     *
     * @param source
     * @param target
     * @param <T>
     * @return
     * @throws Exception
     */
    public static <T> T copyObject(Object source, Class<T> target) {
        T temp = null;
        Class d;
        try {
            temp = target.newInstance(); //用Class对象生成 指定类型的对象实例
            if (null != source) {
                org.springframework.beans.BeanUtils.copyProperties(source, temp); //把源对象的所有属性拷贝到目标对象实例的属性里
                //BeanUtils.copyProperties(Object source, Object target)方法，source对象和target对象相应属性的名称和类型必须都一样才可以成功拷贝属性值
                //BeanUtils.copyProperties只对bean属性进行复制，这里的复制属于浅复制。BeanUtils.copyProperties利用反射，直接将对象的引用set进去，并不是深拷贝。
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return temp;
    }

    /**
     * 拷贝集合
     *
     * @param source
     * @param target
     * @param <T>
     * @param <S>
     * @return
     */
    public static <T, S> List<T> copyList(List<S> source, Class<T> target) {
        List<T> list = new ArrayList<>();
        if (null != source && source.size() > 0) {
            for (Object obj : source) {  //根据Class对象，生成指定类型的对象的实例
                list.add(BeanCopyUtil.copyObject(obj, target));
            }
        }
        return list;
    }


}