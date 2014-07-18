# 复杂表单CUD实现思路
##前端数据
###	1、 id  隐藏字段，用于后台数据关联
### 2、 表单元数据隐藏字段
* \_\_FORM_TYPE\_\_  标记此表单请求操作            create/update
* \_\_FORM_NAME\_\_  标记此表单名称               add/edit/show
* \_\_FORM_FLAG\_\_  标记是表单来的，还是选择来的   true/false
* \_\_ID\_\_         若此对象有id,取自id,否则生成一个惟一的

###3、对象数据结构

    对象
        标量 (id, properties)
        m:1  对象
        1:n,m:n 对象[]

###4、对象列表数据结构
* 对象id,properties
* 对象的所有隐藏字段
* \_\_ACTION\_\_    标记数据状态           create/update/delete
* 数据传输，如果\_\_ACTION\_\_是create/update 需要整个对象，如果是delete，只需要id

##后台处理
* 普通属性毋须处理
* m:1  如果是字符串类型，普通方式处理。如果以后扩展可以传对象过来，就要单独save 1的这一方
* 1:n, m:n 根据\_\_ACTION\_\_ 判断创建，更新或删除，单独循环处理 n 这一方
* 递归处理
* 整理数据保存
