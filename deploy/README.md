### ��ǰĿ¼˵��
**1.** ��ǰĿ¼�µ� jenkins-jobs �� jenkins-libs ����ģ������Jenkins��ʹ�õģ�ǰ����Jenkins��ÿ������������ļ�(JenkinsFile)��һ���ļ��ʹ���һ��Jenkins���񣬺�����Jenkins��ȫ�ֹ����(Global Pipeline Libraries)  
**2.** jenkins-libs Ҫ��Ϊȫ�ֹ���⣬��Ҫ��Jenkins�����ã������������Ϊdeploy-libs����Ϊ�ڴ�����д���˴����ƣ�����·��Ϊ Manage Jenkins -> System Configuration -> Configure System �ҵ� Global Pipeline Libraries��Ȼ�����ù��������ơ�git��ַ �ȵ�  
**3.** jenkins-jobs �� jenkins-libs�����õ���Jenkins�е�����������ߣ�jdk8��maven3����Ҫ����Jenkins�����úã�����·��Ϊ��Manage Jenkins -> System Configuration -> Global Tool Configuration ����������JDK��MAVEN������JDK�ı���Ϊjdk8��maven��NameΪmaven3  
**4.** jenkins-jobs �� jenkins-libs�����õ���Jenkins�е�ȫ�ֻ���������PROFILE ����ǰ����������·��Ϊ��Manage Jenkins -> System Configuration -> Configure System �ҵ� "ȫ������" Ȼ���������  
**5.** k8s-resources ��k8s����ҵ��Ӧ�õ������ļ��������һ���ļ��ʹ������ҵ��Ӧ�õĲ�������

### ����
ʵ��ʹ��ʱ����ѵ�ǰĿ¼�µ�jenkins-jobs��jenkins-libs��k8s-resourcesģ�����һ������git�ֿ��ַ���ɿ�������ά�Ŷӹ�ͬά�����ٰ�ҵ����뵥��һ���⣬��ҵ�񿪷��Ŷ�ά�������������������Ȩ�޿���
