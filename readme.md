# NovaStar
---

A Tiny Flat Distributed File System.

Using React & Node.js & Java EE.

---

## Presentation Layer

网页前端会将所有的用户操作发送到File Transfer Server 处理，

包括：

1.获取所有节点状态

2.设置文件块大小

3.获取所有文件信息及磁盘空间

4.上传文件

5.下载文件

6.重命名文件

7.删除文件

8.格式化文件系统

### 获取所有节点状态

网页会按每16秒向File Transfer Server请求每个节点状态， Server会再向Master请求，获取结果后将解析结果返回到网页上。

### 设置文件块大小

网页上可以设文件块大小，设置之后每一个上传的文件都会按此大小分割。

### 获取所有文件信息及磁盘空间

在网页上点击刷新，网页会向File Transfer Server请求最新的文件列表和磁盘空间，然后Server再向Master发送请求，将Response解析后再返回到网页。

### 上传文件

在网页上传文件，文件会先发送到File Transfer Server， Server 会根据文件大小决定是否分块。
在分割结束后，发送文件名称、文件大小、分块个数、文件块大小到Master。
Master将分配好的虚拟机的IP和路径以及虚拟机的用户名和密码返回回来，File Transfer Server再向返回会来的每一个虚拟机发送相应的文件块。

### 下载文件

网页将要下载的文件ID发送到File Transfer Server，Server 向Master请求该文件的每一块的可用的下载地址，然后Server将每一块从虚拟机中下载下来和并每一块，将合并完整的文件放到FTP服务器上，最后把下载链接返回到网页，网页会自动弹出新的窗口下载该文件。

### 重命名文件

网页将要删除的文件ID发送到File Transfer Server，Server，Server发送重命名请求到Master，Master更改文件名称，将消息返回到File Transfer Server再返回到网页，此时网页会自动刷新文件列表获取最新的所有文件信息。

### 删除文件

网页将要删除的文件ID发送到File Transfer Server，Server 将文件信息中记录的每一块从虚拟机中删除，最后再发送验证请求到Master，待Master确认后，则文件删除成功。

### 格式化文件系统

网页上点击格式化按钮，请求到达File Transfer Server，再转发到Master，Master执行格式化，完成后将消息返回到Server再接着返回到网页上。