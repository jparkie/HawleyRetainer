# HawleyRetainer
**An Android annotation processing and code generation library to retain complex objects which cannot be parceled nor serialized into a `Bundle` across configuration changes.**

It utilizes a retained fragment implementing the `Map` interface to provide identical operations to a `Bundle`. 

It does not behave like a Singleton class as it observes the lifecycle of its bound `Activity`, thus, it destroys itself accordingly. This characteristic is useful to prevent fields being retained in memory beyond the user-initiated destruction of an `Activity` like a Singleton while still being retained during configuration changes. 

Utilize this class as a means of simplifying the retention of certain expensive operations beyond configuration changes while obeying user-initiated lifecycle events such as concurrent, file input-output. or network operations. 

**Nonetheless, as the retained objects cannot be parceled nor serialized, they can still be claimed by the garbage collector resulting in the loss of data.**

```java
public class ExampleActivity extends Activity {
  @HawleyRetain
  Observable<Response> mNetworkResponse;
  
  @Override
  public void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    HawleyRetainer.restoreRetainedObjectMap(this, this);
  }
  
  @Override
  public void onSaveInstanceState(Bundle outState) {
    super.onSaveInstanceState(outState);
    HawleyRetainer.saveRetainedObjectMap(this, this);
  }
}
```

## Usage
```java
@HawleyRetain // A single annotation used to annotate any number of fields which you want to retain.

public static <T> void saveRetainedObjectMap(T target, Activity activity);

public static <T> void restoreRetainedObjectMap(T target, Activity activity);
```

The methods can be called upon any target class as long as an `Activity` can be specified along with it. The retainer will recursively traverse the inheritance graph to inject all annotated fields. Please remember that the support library variants of `Fragment` and `Activity` extend from them. As a result, the methods do not have overloads.

## Download
*Preparing for Maven Central.*

### JitPack

Add the JitPack repository to your root build file:
```groovy
repositories {
  maven {
    url "https://jitpack.io"
  }
}
```

Add the following Gradle plugin to your root build file to support annotation processing:
```groovy
dependencies {
  classpath 'com.neenbedankt.gradle.plugins:android-apt:1.4'
}
```

In your project build file apply the plugin and include the library as a dependency:
```groovy
apply plugin: 'com.neenbedankt.android-apt'

...

dependencies {
  apt 'com.github.jparkie:HawleyRetainer.compiler:1.0.0-RC1-JP'
  compile 'com.github.jparkie:HawleyRetainer.api:1.0.0-RC1-JP'
}
```

**If you are using other annotation processing and code generation libraries like Dagger, do not state the dependency as `provided`. Instead state the dependency as `apt` such that the code generation will run over the classes: `apt 'com.squareup.dagger:dagger-compiler:1.2.2'`.**

## License

    Copyright 2015 Jacob Park
    
    Licensed under the Apache License, Version 2.0 (the "License");
    you may not use this file except in compliance with the License.
    You may obtain a copy of the License at
    
        http://www.apache.org/licenses/LICENSE-2.0
    
    Unless required by applicable law or agreed to in writing, software
    distributed under the License is distributed on an "AS IS" BASIS,
    WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
    See the License for the specific language governing permissions and
    limitations under the License.
