package pl.iterators.kebs.core

import org.scalatest.funsuite.AnyFunSuite
import org.scalatest.matchers.should.Matchers
import pl.iterators.kebs.core.enums.EnumNaming

import java.net.URLClassLoader

object EnumNamingInitializationTests {
  // loads kebs-core again, so that a test can choose which object is initialised first
  private class FreshCore
      extends URLClassLoader(
        Array(classOf[EnumNaming].getProtectionDomain.getCodeSource.getLocation),
        classOf[EnumNaming].getClassLoader
      ) {
    override def loadClass(name: String, resolve: Boolean): Class[_] = getClassLoadingLock(name).synchronized {
      if (name.startsWith("pl.iterators.kebs.core.")) Option(findLoadedClass(name)).getOrElse(findClass(name))
      else super.loadClass(name, resolve)
    }

    def default(objectName: String): AnyRef = {
      val module = Class.forName("pl.iterators.kebs.core.enums." + objectName + "$", true, this)
      module.getMethod("default").invoke(module.getField("MODULE$").get(null))
    }
  }
}

// a cycle between the two objects also deadlocks threads that initialise them at the same time
class EnumNamingInitializationTests extends AnyFunSuite with Matchers {
  import EnumNamingInitializationTests._

  test("the default is there when the implicit one is used first") {
    val core            = new FreshCore
    val implicitDefault = core.default("EnumNamingDefaults")
    implicitDefault should not be null
    core.default("EnumNaming") should be theSameInstanceAs implicitDefault
  }

  test("the default is there when EnumNaming is used first") {
    val core            = new FreshCore
    val explicitDefault = core.default("EnumNaming")
    explicitDefault should not be null
    core.default("EnumNamingDefaults") should be theSameInstanceAs explicitDefault
  }
}
