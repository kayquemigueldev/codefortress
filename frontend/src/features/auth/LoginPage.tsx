import { zodResolver } from '@hookform/resolvers/zod'
import { useState } from 'react'
import {
    Link,
    useLocation,
    useNavigate,
} from 'react-router'
import {
    useForm,
} from 'react-hook-form'
import { z } from 'zod'

import { ApiError } from '../../lib/api/api-error'
import { useAuth } from '../../lib/auth/useAuth'

const loginSchema = z.object({
    email: z
        .string()
        .trim()
        .min(
            1,
            'Enter your email.',
        )
        .email(
            'Enter a valid email.',
        )
        .max(
            320,
            'Email is too long.',
        ),

    password: z
        .string()
        .min(
            1,
            'Enter your password.',
        )
        .max(
            72,
            'Password is too long.',
        ),
})

type LoginFormData =
    z.infer<typeof loginSchema>

type LoginLocationState = {
    from?: string
}

export function LoginPage() {
    const { login } = useAuth()

    const navigate = useNavigate()
    const location = useLocation()

    const [serverError, setServerError] =
        useState<string | null>(null)

    const {
        register,
        handleSubmit,
        formState: {
            errors,
            isSubmitting,
        },
    } = useForm<LoginFormData>({
        resolver: zodResolver(
            loginSchema,
        ),

        defaultValues: {
            email: '',
            password: '',
        },
    })

    const state =
        location.state as
            | LoginLocationState
            | null

    const destination =
        state?.from
        ?? '/app/dashboard'

    async function onSubmit(
        data: LoginFormData,
    ) {
        setServerError(null)

        try {
            await login(data)

            navigate(
                destination,
                {
                    replace: true,
                },
            )
        } catch (error) {
            if (error instanceof ApiError) {
                setServerError(
                    error.message,
                )

                return
            }

            setServerError(
                'Unable to sign in. Try again.',
            )
        }
    }

    return (
        <main className="auth-page">
            <section className="auth-card">
                <div className="auth-brand">
          <span className="auth-brand__mark">
            CF
          </span>

                    <span>CodeFortress</span>
                </div>

                <header className="auth-header">
                    <p className="auth-eyebrow">
                        Secure workspace
                    </p>

                    <h1>
                        Welcome back.
                    </h1>

                    <p>
                        Sign in to review projects,
                        analyses and security findings.
                    </p>
                </header>

                <form
                    className="auth-form"
                    onSubmit={
                        handleSubmit(onSubmit)
                    }
                    noValidate
                >
                    {serverError && (
                        <div
                            className="auth-error"
                            role="alert"
                        >
                            {serverError}
                        </div>
                    )}

                    <div className="auth-field">
                        <label htmlFor="email">
                            Email
                        </label>

                        <input
                            id="email"
                            type="email"
                            autoComplete="email"
                            placeholder="you@example.com"
                            aria-invalid={
                                Boolean(errors.email)
                            }
                            {...register('email')}
                        />

                        {errors.email && (
                            <span className="field-error">
                {errors.email.message}
              </span>
                        )}
                    </div>

                    <div className="auth-field">
                        <label htmlFor="password">
                            Password
                        </label>

                        <input
                            id="password"
                            type="password"
                            autoComplete="current-password"
                            placeholder="Enter your password"
                            aria-invalid={
                                Boolean(
                                    errors.password,
                                )
                            }
                            {...register('password')}
                        />

                        {errors.password && (
                            <span className="field-error">
                {
                    errors.password
                        .message
                }
              </span>
                        )}
                    </div>

                    <button
                        className="auth-submit"
                        type="submit"
                        disabled={isSubmitting}
                    >
                        {isSubmitting
                            ? 'Signing in...'
                            : 'Sign in'}
                    </button>
                </form>

                <footer className="auth-footer">
          <span>
            New to CodeFortress?
          </span>

                    <Link to="/register">
                        Create account
                    </Link>
                </footer>
            </section>

            <aside className="auth-visual">
                <div className="auth-visual__content">
          <span className="auth-visual__badge">
            SECURITY ANALYSIS
          </span>

                    <h2>
                        Find weaknesses before
                        they become incidents.
                    </h2>

                    <p>
                        Analyze source code,
                        configurations and
                        dependencies from one
                        focused workspace.
                    </p>

                    <div className="auth-terminal">
                        <div className="auth-terminal__top">
                            <span />
                            <span />
                            <span />

                            <small>
                                latest-analysis
                            </small>
                        </div>

                        <div className="auth-terminal__body">
                            <p>
                                <span>$</span>
                                {' '}
                                scanning project...
                            </p>

                            <p>
                                <span>✓</span>
                                {' '}
                                148 files inspected
                            </p>

                            <p>
                                <span>!</span>
                                {' '}
                                3 findings detected
                            </p>

                            <p>
                                <span>→</span>
                                {' '}
                                security score: 74
                            </p>
                        </div>
                    </div>
                </div>
            </aside>
        </main>
    )
}
