import { zodResolver } from '@hookform/resolvers/zod'
import {
    useMutation,
    useQuery,
    useQueryClient,
} from '@tanstack/react-query'
import {
    Link,
    useNavigate,
    useParams,
} from 'react-router'
import {
    useEffect,
} from 'react'
import {
    useForm,
} from 'react-hook-form'
import { z } from 'zod'

import { ApiError } from '../../lib/api/api-error'

import {
    getProject,
    updateProject,
} from './project-api'

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

export function EditProjectPage() {
    const { projectId } =
        useParams<{
            projectId: string
        }>()

    const navigate = useNavigate()
    const queryClient = useQueryClient()

    const projectQuery = useQuery({
        queryKey: [
            'projects',
            projectId,
        ],

        queryFn: () =>
            getProject(
                projectId ?? '',
            ),

        enabled:
            Boolean(projectId),
    })

    const {
        register,
        handleSubmit,
        reset,
        setError,
        formState: {
            errors,
            isDirty,
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

    useEffect(() => {
        if (!projectQuery.data) {
            return
        }

        reset({
            name:
            projectQuery.data.name,

            description:
                projectQuery.data
                    .description
                ?? '',
        })
    }, [
        projectQuery.data,
        reset,
    ])

    const updateMutation = useMutation({
        mutationFn: (
            data: ProjectFormData,
        ) =>
            updateProject(
                projectId ?? '',
                {
                    name: data.name,
                    description:
                        data.description
                        || null,
                },
            ),

        onSuccess: async (
            updatedProject,
        ) => {
            await Promise.all([
                queryClient.invalidateQueries({
                    queryKey: ['projects'],
                }),

                queryClient.invalidateQueries({
                    queryKey: [
                        'projects',
                        projectId,
                    ],
                }),

                queryClient.invalidateQueries({
                    queryKey: ['dashboard'],
                }),
            ])

            navigate(
                `/app/projects/${updatedProject.id}`,
                {
                    replace: true,
                },
            )
        },
    })

    async function onSubmit(
        data: ProjectFormData,
    ) {
        updateMutation.reset()

        try {
            await updateMutation.mutateAsync(
                data,
            )
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
                        'Unable to update project. Try again.',
                },
            )
        }
    }

    if (!projectId) {
        return (
            <main className="projects-page">
                <div className="project-form-container">
                    <h1>
                        Invalid project.
                    </h1>
                </div>
            </main>
        )
    }

    if (projectQuery.isPending) {
        return (
            <main className="projects-page">
                <div className="project-form-container">
                    <p className="projects-eyebrow">
                        Edit project
                    </p>

                    <h1>
                        Loading project...
                    </h1>
                </div>
            </main>
        )
    }

    if (projectQuery.isError) {
        const projectNotFound =
            projectQuery.error
            instanceof ApiError
            && projectQuery.error.status
            === 404

        return (
            <main className="projects-page">
                <div className="project-form-container">
                    <p className="projects-eyebrow">
                        Edit project
                    </p>

                    <h1>
                        {projectNotFound
                            ? 'Project not found.'
                            : 'Unable to load project.'}
                    </h1>

                    <p className="projects-description">
                        {projectNotFound
                            ? 'This project does not exist or does not belong to your account.'
                            : 'Something went wrong while loading this project.'}
                    </p>

                    {!projectNotFound && (
                        <button
                            className="projects-primary-button"
                            type="button"
                            onClick={() => {
                                void projectQuery.refetch()
                            }}
                        >
                            Try again
                        </button>
                    )}

                    <Link
                        className="project-back-link project-details-back"
                        to="/app/projects"
                    >
                        ← Back to projects
                    </Link>
                </div>
            </main>
        )
    }

    const project =
        projectQuery.data

    return (
        <main className="projects-page">
            <div className="project-form-container">
                <Link
                    className="project-back-link"
                    to={
                        `/app/projects/${project.id}`
                    }
                >
                    ← Back to project
                </Link>

                <header className="project-form-header">
                    <p className="projects-eyebrow">
                        Edit project
                    </p>

                    <h1>
                        Update codebase.
                    </h1>

                    <p>
                        Change the project name
                        or description. Existing
                        analyses and findings will
                        remain associated with this
                        project.
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
                            to={
                                `/app/projects/${project.id}`
                            }
                        >
                            Cancel
                        </Link>

                        <button
                            className="projects-primary-button"
                            type="submit"
                            disabled={
                                updateMutation.isPending
                                || !isDirty
                            }
                        >
                            {updateMutation.isPending
                                ? 'Saving changes...'
                                : 'Save changes'}
                        </button>
                    </div>
                </form>
            </div>
        </main>
    )
}