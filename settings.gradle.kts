enableFeaturePreview("TYPESAFE_PROJECT_ACCESSORS")

rootProject.name = "annoy4s-kt"
include("annoy4s-native")
project(":annoy4s-native").name = "annoy"
include("annoy4s-jna")
project(":annoy4s-jna").name = "annoy4s"
