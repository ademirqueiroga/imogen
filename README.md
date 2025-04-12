# Imogen

Imogen is a Kotlin Symbol Processing (KSP) processor that generates implementations of interfaces with additional functionality. It simplifies the creation of model/data classes by generating concrete implementations from interface definitions.

## Features

- Automatically generates implementation classes from interfaces
- Customizable class naming with prefixes and suffixes
- Support for default values of various types
- Clean, boilerplate-free model definitions

## Installation

### Gradle Setup

Add KSP and Imogen to your project:

```kotlin
// Root build.gradle.kts
plugins {
    id("com.google.devtools.ksp") version "YOUR_KSP_VERSION" apply false
    kotlin("jvm") version "YOUR_KOTLIN_VERSION" apply false
}

// Module build.gradle.kts
plugins {
    kotlin("jvm")
    id("com.google.devtools.ksp")
}

dependencies {
    implementation("me.ademirqueiroga:imogen:0.9.0-SNAPSHOT") 
    ksp("me.ademirqueiroga:imogen:0.9.0-SNAPSHOT")
}
```

## Usage

1. Define your model as an interface with the `@Generate` annotation:

```kotlin
import annotations.Generate

@Generate(name = "UserImpl")
interface User {
    val id: String
    val name: String
    val email: String
    val age: Int
}
```

2. The processor will automatically generate an implementation class:

```kotlin
// Generated code
class UserImpl(
    override val id: String,
    override val name: String,
    override val email: String,
    override val age: Int
) : User
```

### Customization Options

You can customize the generated class name:

```kotlin
@Generate(
    name = "MyUser",       // Required: Name of the implementation class
    prefix = "Concrete",   // Optional: Prefix for the class name
    suffix = "Model"       // Optional: Suffix for the class name
)
```

This would generate `ConcreteMyUserModel`.

### Default Values

You can specify default values using annotations:

```kotlin
import annotations.*

@Generate(name = "UserImpl")
interface User {
    @StringDefault("Anonymous")
    val name: String
    
    @IntDefault(18)
    val age: Int
    
    @BooleanDefault(true)
    val isActive: Boolean
}
```

## Example

See the `sample` directory for a complete example:

```kotlin
import annotations.Generate

@Generate(name = "SampleImpl")
interface Sample {
    val string: String
    val int: Int
    val long: Long
    val double: Double
    val float: Float
    val sample: Sample
}
```

## Development Status

This project is still a work in progress. Future updates may include:
- More customization options
- Additional annotations
- Enhanced documentation

## License

This project is licensed under the MIT License - see the [LICENSE](LICENSE) file for details.

## Author

Ademir Queiroga (admqueiroga@gmail.com)
