# Functions (`kmath-functions`)

Functions and interpolations:

 - [piecewise](Piecewise functions.) : src/commonMain/kotlin/kscience/kmath/functions/Piecewise.kt
 - [polynomials](Polynomial functions.) : src/commonMain/kotlin/kscience/kmath/functions/Polynomial.kt
 - [linear interpolation](Linear XY interpolator.) : src/commonMain/kotlin/kscience/kmath/interpolation/LinearInterpolator.kt
 - [spline interpolation](Cubic spline XY interpolator.) : src/commonMain/kotlin/kscience/kmath/interpolation/SplineInterpolator.kt


> #### Artifact:
>
> This module artifact: `space.kscience:kmath-functions:0.2.0`.
>
> Bintray release version:        [ ![Download](https://api.bintray.com/packages/mipt-npm/kscience/kmath-functions/images/download.svg) ](https://bintray.com/mipt-npm/kscience/kmath-functions/_latestVersion)
>
> Bintray development version:    [ ![Download](https://api.bintray.com/packages/mipt-npm/dev/kmath-functions/images/download.svg) ](https://bintray.com/mipt-npm/dev/kmath-functions/_latestVersion)
>
> **Gradle:**
>
> ```gradle
> repositories {
>     maven { url 'https://repo.kotlin.link' }
>     maven { url 'https://dl.bintray.com/hotkeytlt/maven' }
>     maven { url "https://dl.bintray.com/kotlin/kotlin-eap" } // include for builds based on kotlin-eap
>//     Uncomment if repo.kotlin.link is unavailable 
>//     maven { url 'https://dl.bintray.com/mipt-npm/kscience' }
>//     maven { url 'https://dl.bintray.com/mipt-npm/dev' }
> }
> 
> dependencies {
>     implementation 'space.kscience:kmath-functions:0.2.0'
> }
> ```
> **Gradle Kotlin DSL:**
>
> ```kotlin
> repositories {
>     maven("https://repo.kotlin.link")
>     maven("https://dl.bintray.com/kotlin/kotlin-eap") // include for builds based on kotlin-eap
>     maven("https://dl.bintray.com/hotkeytlt/maven") // required for a
>//     Uncomment if repo.kotlin.link is unavailable 
>//     maven("https://dl.bintray.com/mipt-npm/kscience")
>//     maven("https://dl.bintray.com/mipt-npm/dev")
> }
> 
> dependencies {
>     implementation("space.kscience:kmath-functions:0.2.0")
> }
> ```
