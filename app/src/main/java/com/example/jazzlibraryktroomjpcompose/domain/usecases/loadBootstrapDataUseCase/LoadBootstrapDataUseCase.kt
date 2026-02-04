package com.example.jazzlibraryktroomjpcompose.domain.usecases.loadBootstrapDataUseCase

class LoadBootstrapDataUseCase(
    private val repository: com.example.jazzlibraryktroomjpcompose.data.repository.JazzRepositoryImpl
) {
    suspend operator fun invoke(): Result<Unit> {
        return repository.loadBootstrapData()
    }
}