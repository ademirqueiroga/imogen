package processor

import com.google.devtools.ksp.processing.SymbolProcessor
import com.google.devtools.ksp.processing.SymbolProcessorEnvironment
import com.google.devtools.ksp.processing.SymbolProcessorProvider
import processor.ImogenSymbolProcessor


class ImogenSymbolProcessorProvider : SymbolProcessorProvider {
    override fun create(environment: SymbolProcessorEnvironment): SymbolProcessor {
        return ImogenSymbolProcessor(
            environment.codeGenerator,
            environment.logger,
        )
    }
}
