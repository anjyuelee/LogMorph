plugins {
    alias(libs.plugins.android.library)
    alias(libs.plugins.kotlin.android)
    `maven-publish`
    signing
}

android {
    namespace = "com.anjyue.logmorph.logger"
    compileSdk = 34

    defaultConfig {
        minSdk = 24

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
        consumerProguardFiles("consumer-rules.pro")
    }

    buildTypes {
        release {
            isMinifyEnabled = false
            proguardFiles(
                getDefaultProguardFile("proguard-android-optimize.txt"),
                "proguard-rules.pro"
            )
        }
    }
    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_11
        targetCompatibility = JavaVersion.VERSION_11
    }
    kotlinOptions {
        jvmTarget = "11"
    }
    publishing {
        singleVariant("release") {
            withSourcesJar()
            withJavadocJar()
        }
    }
}

dependencies {
    implementation(libs.androidx.core.ktx)
    implementation(libs.androidx.lifecycle.runtime.ktx)
    api(libs.okhttp)
    testImplementation(libs.junit)
    androidTestImplementation(libs.androidx.junit)
    androidTestImplementation(libs.androidx.espresso.core)
}

group = property("GROUP") as String
version = property("VERSION_NAME") as String

publishing {
    publications {
        register<MavenPublication>("release") {
            from(components["release"])
            groupId = group.toString()
            artifactId = "logger"
            version = version.toString()

            pom {
                name.set(property("POM_NAME") as String)
                description.set(property("POM_DESCRIPTION") as String)
                url.set(property("POM_URL") as String)
                licenses {
                    license {
                        name.set(property("POM_LICENSE_NAME") as String)
                        url.set(property("POM_LICENSE_URL") as String)
                        distribution.set(property("POM_LICENSE_DIST") as String)
                    }
                }
                developers {
                    developer {
                        id.set(property("POM_DEVELOPER_ID") as String)
                        name.set(property("POM_DEVELOPER_NAME") as String)
                        email.set(property("POM_DEVELOPER_EMAIL") as String)
                    }
                }
                scm {
                    url.set(property("POM_SCM_URL") as String)
                    connection.set(property("POM_SCM_CONNECTION") as String)
                    developerConnection.set(property("POM_SCM_DEV_CONNECTION") as String)
                }
            }
        }
    }
    repositories {
        maven {
            name = "sonatype"
            url = uri("https://s01.oss.sonatype.org/service/local/staging/deploy/maven2/")
            credentials {
                username = findProperty("ossrhUsername") as String?
                password = findProperty("ossrhPassword") as String?
            }
        }
        maven {
            name = "localTest"
            url = uri(layout.buildDirectory.dir("repo"))
        }
    }
}

signing {
    sign(publishing.publications["release"])
}
