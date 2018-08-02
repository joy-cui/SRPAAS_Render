### SRPAAS_Render 

####渲染功能模块




### 渲染功能模块
渲染功能模块，通过openGL渲染yuv的功能，可实现缩放功能；
通过调用renderer.update(y, u, v)实现视频显示效果；

### 集成说明
直接将SRPAAS_Render目录下的.jar文件拷贝到application项目的libs目录下，并将在app目录下的build.gradle中配置：

dependencies {

    compile fileTree(include: ['*.jar'], dir: 'libs')

    compile files('libs/SRPAAS_Render_v0.1.0.jar') //对应libs中的jar文件
    ##//如果编译出错，将上面的配置修改为
    provided fileTree(include: ['*.jar'], dir: 'libs')
}

