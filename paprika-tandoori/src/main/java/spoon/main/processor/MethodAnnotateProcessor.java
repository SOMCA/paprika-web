package spoon.main.processor;

import java.util.Set;

import spoon.reflect.declaration.CtMethod;
import spoon.reflect.factory.AnnotationFactory;

/**
 * Add annotations for method
 * 
 * @author guillaume
 *
 */
// We use many rawtypes.
@SuppressWarnings("rawtypes")
public class MethodAnnotateProcessor {

	private CtMethod element;
	private String qualifiedName;
	private AnnotationFactory factory;

	/**
	 * @param element
	 * @param currentClass
	 */
	public void process(CtMethod element, String currentClass) {
		String qualifiedName = element.getSimpleName() + "#" + currentClass;

		AnnotationFactory factory = new AnnotationFactory(element.getFactory());
		this.element = element;
		this.qualifiedName = qualifiedName;
		this.factory = factory;

		longMethodDetection("LM_NO_FUZZY");
		longMethodDetection("LM");
		getterSetterDetection("IGS");
		hashMapUsageDetection("HMU");
		heavyAsyncTaskStepsDetection("HAS_NO_FUZZY");
		heavyAsyncTaskStepsDetection("HAS");
		heavyBroadcastReceiverDetection("HBR_NO_FUZZY");
		heavyBroadcastReceiverDetection("HBR");
		heavyServiceStartDetection("HSS_NO_FUZZY");
		heavyServiceStartDetection("HSS");
		initOnDrawDetection("IOD");
		invalidateWithoutRectDetection("IWR");
		mimDetection("MIM");
		overdrawDetection("UIO");
		trackingHardwareIdDetection("THI");
		unsuitedLRUCacheSizeDetection("UCS");
		unsupportedHardwareAccelerationDetection("UHA");
	}

	private boolean isOnSet(String codesmells) {
		Set<String> valueSet = AnnotateProcessor.codesmells.get(codesmells);
		return (valueSet != null && valueSet.contains(qualifiedName));
	}

	private void getterSetterDetection(String codesmells) {
		if (isOnSet(codesmells)) {
			Class<codesmells.annotations.IGS> annotationType = codesmells.annotations.IGS.class;
			factory.annotate(element, annotationType);
		}

	}

	private void longMethodDetection(String codesmells) {
		if (isOnSet(codesmells)) {
			Class<codesmells.annotations.LM> annotationType = codesmells.annotations.LM.class;
			factory.annotate(element, annotationType);
		}
	}

	private void hashMapUsageDetection(String codesmells) {
		if (isOnSet(codesmells)) {
			Class<codesmells.annotations.HMU> annotationType = codesmells.annotations.HMU.class;
			factory.annotate(element, annotationType);
		}
	}

	private void heavyAsyncTaskStepsDetection(String codesmells) {
		if (isOnSet(codesmells)) {
			Class<codesmells.annotations.HAS> annotationType = codesmells.annotations.HAS.class;
			factory.annotate(element, annotationType);
		}
	}

	private void heavyBroadcastReceiverDetection(String codesmells) {
		if (isOnSet(codesmells)) {
			Class<codesmells.annotations.HBR> annotationType = codesmells.annotations.HBR.class;
			factory.annotate(element, annotationType);
		}
	}

	private void heavyServiceStartDetection(String codesmells) {
		if (isOnSet(codesmells)) {
			Class<codesmells.annotations.HSS> annotationType = codesmells.annotations.HSS.class;
			factory.annotate(element, annotationType);
		}
	}

	private void initOnDrawDetection(String codesmells) {
		if (isOnSet(codesmells)) {
			Class<codesmells.annotations.IOD> annotationType = codesmells.annotations.IOD.class;
			factory.annotate(element, annotationType);
		}
	}

	private void invalidateWithoutRectDetection(String codesmells) {
		if (isOnSet(codesmells)) {
			Class<codesmells.annotations.IWR> annotationType = codesmells.annotations.IWR.class;
			factory.annotate(element, annotationType);
		}
	}

	private void mimDetection(String codesmells) {
		if (isOnSet(codesmells)) {
			Class<codesmells.annotations.MIM> annotationType = codesmells.annotations.MIM.class;
			factory.annotate(element, annotationType);
		}
	}

	private void overdrawDetection(String codesmells) {
		if (isOnSet(codesmells)) {
			Class<codesmells.annotations.UIO> annotationType = codesmells.annotations.UIO.class;
			factory.annotate(element, annotationType);
		}
	}

	private void trackingHardwareIdDetection(String codesmells) {
		if (isOnSet(codesmells)) {
			Class<codesmells.annotations.THI> annotationType = codesmells.annotations.THI.class;
			factory.annotate(element, annotationType);
		}
	}

	private void unsuitedLRUCacheSizeDetection(String codesmells) {
		if (isOnSet(codesmells)) {
			Class<codesmells.annotations.UCS> annotationType = codesmells.annotations.UCS.class;
			factory.annotate(element, annotationType);
		}
	}

	private void unsupportedHardwareAccelerationDetection(String codesmells) {
		if (isOnSet(codesmells)) {
			Class<codesmells.annotations.UHA> annotationType = codesmells.annotations.UHA.class;
			factory.annotate(element, annotationType);
		}
	}

}