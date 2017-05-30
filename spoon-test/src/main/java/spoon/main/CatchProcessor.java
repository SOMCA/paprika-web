package spoon.main;

import org.apache.log4j.Level;

import spoon.processing.AbstractProcessor;
import spoon.reflect.code.CtCatch;
import spoon.reflect.code.CtComment;

/**
 * Reports warnings when empty catch blocks are found.
 */
public class CatchProcessor extends AbstractProcessor<CtCatch> {
	public void process(CtCatch element) {
		if (element.getBody().getStatements().size() == 0) {
			getFactory().getEnvironment().report(this, Level.WARN, element, "empty catch clause");
	
			element.addComment(element.getFactory().Code().createComment("comment", CtComment.CommentType.INLINE));

		}
	}
}