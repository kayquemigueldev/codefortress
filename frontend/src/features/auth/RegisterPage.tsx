import { zodResolver } from '@hookform/resolvers/zod'
import { useState } from 'react'
import {
    Link,
    useNavigate,
} from 'react-router'
import { useForm } from 'react-hook-form'
import { z } from 'zod'

import { ApiError } from '../../lib/api/api-error'
import { useAuth } from '../../lib/auth/useAuth'

const registerSchema = z
    .object({
        displayName: z
            .string()
            .trim()
            .min(
                1,
                'Enter your name.',
            )
            .max(
                120,
                'Name is too long.',
            ),

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
                12,
                'Password must contain at least 12 characters.',
            )
            .max(
                72,
                'Password must contain at most 72 characters.',
            ),

        confirmPassword: z
            .string()
            .min(
                1,
                'Confirm your password.',
            ),
    })
    .refine(
        (data) =>
            data.password
            === data.confirmPassword,
        {
            message:
                'Passwords do not match.',
            path: ['confirmPassword'],
        },
    )

type RegisterFormData =
    z.infer<typeof registerSchema>

export function RegisterPage() {
    const { register: createAccount } =
        useAuth()

    const navigate = useNavigate()

    const [serverError, setServerError] =
        useState<string | null>(null)

    const {
        register,
        handleSubmit,
        formState: {
            errors,
            isSubmitting,
        },
    } = useForm<RegisterFormData>({
        resolver: zodResolver(
            registerSchema,
        ),

        defaultValues: {
            displayName: '',
            email: '',
            password: '',
            confirmPassword: '',
        },
    })

    async function onSubmit(
        data: RegisterFormData,
    ) {
        setServerError(null)

        try {
            await createAccount({
                displayName:
                data.displayName,
                email: data.email,
                password: data.password,
            })

            navigate(
                '/login',
                {
                    replace: true,
                    state: {
                        registered: true,
                    },
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
                'Unable to create account. Try again.',
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
                        Create workspace
                    </p>

                    <h1>
                        Start securing your code.
                    </h1>

                    <p>
                        Create your account to
                        analyze projects and review
                        security findings.
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
                        <label htmlFor="displayName">
                            Name
                        </label>

                        <input
                            id="displayName"
                            type="text"
                            autoComplete="name"
                            placeholder="Your name"
                            aria-invalid={
                                Boolean(
                                    errors.displayName,
                                )
                            }
                            {...register(
                                'displayName',
                            )}
                        />

                        {errors.displayName && (
                            <span className="field-error">
                {
                    errors.displayName
                        .message
                }
              </span>
                        )}
                    </div>

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
                            autoComplete="new-password"
                            placeholder="At least 12 characters"
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

                    <div className="auth-field">
                        <label
                            htmlFor="confirmPassword"
                        >
                            Confirm password
                        </label>

                        <input
                            id="confirmPassword"
                            type="password"
                            autoComplete="new-password"
                            placeholder="Repeat your password"
                            aria-invalid={
                                Boolean(
                                    errors.confirmPassword,
                                )
                            }
                            {...register(
                                'confirmPassword',
                            )}
                        />

                        {errors.confirmPassword && (
                            <span className="field-error">
                {
                    errors.confirmPassword
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
                            ? 'Creating account...'
                            : 'Create account'}
                    </button>
                </form>

                <footer className="auth-footer">
          <span>
            Already have an account?
          </span>

                    <Link to="/login">
                        Sign in
                    </Link>
                </footer>
            </section>

            <aside className="auth-visual">
                <div className="auth-visual__content">
          <span className="auth-visual__badge">
            CODEFORTRESS
          </span>

                    <h2>
                        Security belongs in
                        every development cycle.
                    </h2>

                    <p>
                        Discover exposed secrets,
                        risky configurations and
                        vulnerable code before
                        release.
                    </p>

                    <div className="auth-terminal">
                        <div className="auth-terminal__top">
                            <span />
                            <span />
                            <span />

                            <small>
                                security-check
                            </small>
                        </div>

                        <div className="auth-terminal__body">
                            <p>
                                <span>✓</span>
                                {' '}
                                source discovered
                            </p>

                            <p>
                                <span>✓</span>
                                {' '}
                                rules executed
                            </p>

                            <p>
                                <span>!</span>
                                {' '}
                                findings classified
                            </p>

                            <p>
                                <span>→</span>
                                {' '}
                                remediation ready
                            </p>
                        </div>
                    </div>
                </div>
            </aside>
        </main>
    )
}