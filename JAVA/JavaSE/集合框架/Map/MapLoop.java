public class MapLoop {

  /**
  * 第一种：通过Map.keySet()遍历key和value
  */
  public void outMapByKeySet(Map map){
    for (String key : map.keySet()) {
      String value = map.get(key).toString();
    }
  }

  /**
  * 第二种：通过Map.entrySet使用iterator遍历key和value
  */
  public void outMapByIterator(Map map){
    Iterator<Entry<String, Object>> it = map.entrySet().Iterator();
    while(it.hasNext()){
      Entry<String, Object> entry = it.next();
      System.out.println("key:"+entry.getKey()+", value:"+entry.getValue());
    }
  }

  /**
  * 第三种：通过Map.entrySet遍历key和value
  * Map集合循环遍历方式三 推荐，尤其是容量大时
  */
  public void outMapByEntrySet(Map map){
    for (Map.Entry<String, Object> m : map.entrySet()) {
      System.out.println("key:"+m.getKey()+", value:"+m.getValue());
    }
  }

  /**
  * 第四种：通过Map.values()遍历所有的value，但不能遍历key
  */
  public void outMapValues(Map map){
    for (Object value : map.values()) {
      System.out.println("value:"+value);
    }
  }

  public static void main(String[] args) {

  }
}
