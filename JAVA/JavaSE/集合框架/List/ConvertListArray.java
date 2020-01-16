public class ConvertListArray {
  public static List ArrayToList(Object[] objects){
    List result = null;
    // 1、for循环add


    // 2、Arrays.asList方法：不能使用其修改集合相关的方法（add/remove/clear方法）
    //result = Arrays.asList(objects);
    // 2-1变种Arrays.asList方法：能使用其修改集合相关的方法（add/remove/clear方法）
    result = new ArrayList(Arrays.asList(objects));

    // 3、Collections.addAll()
    result = new ArrayList();
    Collections.addAll(result, objects);

    // 4、Java8 数组转为List：流的方式
    //result = Stream.of(objects).collect(Collectors.toList());

    return result;
  }

  public static Object[] ListToArray(List list){
    Object[] result;
    // 1、for循环

    // 2、list.toArray方法
    result = list.toArray(new String[list.size()]);

    // 3、Java8 List转为数组
    //result = list.stream().toArray(String[]::new);
    //Arrays.stream(result).forEach(str -> System.err.println(str));

    return result;
  }
}
