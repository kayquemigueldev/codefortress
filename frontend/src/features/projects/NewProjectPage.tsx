import { zodResolver } from '@hookform/resolvers/zod'
import {
    useMutation,
    useQueryClient,
} from '@tanstack/react-query'
import {
    Link,
    useNavigate,
} from 'react-router'
import { useForm } from 'react-hook-form'
import { z } from 'zod'

import { ApiError } from '../../lib/api/api-error'

import { createProject } from './project-api'

const projectSchema = z.object({
    name: z
        .string()
        .trim()
        .min(
            1,
            'Enter a project name.',
        )
        .max(
            120,
            'Project name must have at most 120 characters.',
        ),

    description: z
        .string()
        .trim()
        .max(
            500,
            'Description must have at most 500 characters.',
        ),
})

type ProjectFormData =
    z.infer<typeof projectSchema>

export function NewProjectPage() {
    const navigate = useNavigate()
    const queryClient = useQueryClient()

    const {
        register,
        handleSubmit,
        setError,
        formState: {
            errors,
        },
    } = useForm<ProjectFormData>({
        resolver: zodResolver(
            projectSchema,
        ),

        defaultValues: {
            name: '',
            description: '',
        },
    })

    const createMutation = useMutation({
        mutationFn: createProject,

        onSuccess: async () => {
            await queryClient.invalidateQueries({
                queryKey: ['projects'],
            })

            navigate(
                '/app/projects',
                {
                    replace: true,
                },
            )
        },
    })

    async function onSubmit(
        data: ProjectFormData,
    ) {
        createMutation.reset()

        try {
            await createMutation.mutateAsync({
                name: data.name,
                description:
                    data.description
                    || null,
            })
        } catch (error) {
            if (error instanceof ApiError) {
                setError(
                    'root.server',
                    {
                        message:
                        error.message,
                    },
                )

                return
            }

            setError(
                'root.server',
                {
                    message:
                        'Unable to create project. Try again.',
                },
            )
        }
    }

    return (
        <main className="projects-page">
            <div className="project-form-container">
                <Link
                    className="project-back-link"
                    to="/app/projects"
                >
                    ← Back to projects
                </Link>

                <header className="project-form-header">
                    <p className="projects-eyebrow">
                        New project
                    </p>

                    <h1>
                        Add a codebase.
                    </h1>

                    <p>
                        Create a project now.
                        Source code and analysis
                        configuration will come
                        next.
                    </p>
                </header>

                <form
                    className="project-form"
                    onSubmit={
                        handleSubmit(onSubmit)
                    }
                    noValidate
                >
                    {errors.root?.server && (
                        <div
                            className="project-form-error"
                            role="alert"
                        >
                            {
                                errors.root.server
                                    .message
                            }
                        </div>
                    )}

                    <div className="project-form-field">
                        <div className="project-form-label">
                            <label htmlFor="name">
                                Project name
                            </label>

                            <span>
                Required
              </span>
                        </div>

                        <input
                            id="name"
                            type="text"
                            autoComplete="off"
                            placeholder="CodeFortress"
                            maxLength={120}
                            aria-invalid={
                                Boolean(errors.name)
                            }
                            {...register('name')}
                        />

                        {errors.name && (
                            <span className="field-error">
                {errors.name.message}
              </span>
                        )}
                    </div>

                    <div className="project-form-field">
                        <div className="project-form-label">
                            <label htmlFor="description">
                                Description
                            </label>

                            <span>
                Optional
              </span>
                        </div>

                        <textarea
                            id="description"
                            rows={6}
                            maxLength={500}
                            placeholder="Describe what this project does..."
                            aria-invalid={
                                Boolean(
                                    errors.description,
                                )
                            }
                            {...register(
                                'description',
                            )}
                        />

                        {errors.description && (
                            <span className="field-error">
                {
                    errors.description
                        .message
                }
              </span>
                        )}
                    </div>

                    <div className="project-form-actions">
                        <Link
                            className="project-secondary-button"
                            to="/app/projects"
                        >
                            Cancel
                        </Link>

                        <button
                            className="projects-primary-button"
                            type="submit"
                            disabled={
                                createMutation.isPending
                            }
                        >
                            {createMutation.isPending
                                ? 'Creating project...'
                                : 'Create project'}
                        </button>
                    </div>
                </form>
            </div>
        </main>
    )
}