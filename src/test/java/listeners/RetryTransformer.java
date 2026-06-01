package listeners;

import java.lang.reflect.Constructor;
import java.lang.reflect.Method;

import org.testng.IAnnotationTransformer;
import org.testng.annotations.ITestAnnotation;

@SuppressWarnings("rawtypes")
public class RetryTransformer implements IAnnotationTransformer {

	@Override
	public void transform(ITestAnnotation annotation, Class testClass, Constructor testConstructor, Method testMethod) {
		if (testMethod != null && testMethod.getName().equals("investFlowTest")) {
			return;
		}
		annotation.setRetryAnalyzer(RetryAnalyzer.class);
	}

}
