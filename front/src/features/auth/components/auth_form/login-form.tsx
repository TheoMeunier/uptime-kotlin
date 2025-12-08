import {cn} from "@/lib/utils"
import {
    Card,
    CardContent,
    CardDescription,
    CardHeader,
    CardTitle,
} from "@/components/atoms/card"
import {
    Field,
    FieldGroup,
    FieldLabel,
} from "@/components/atoms/field"
import {Input} from "@/components/atoms/input"
import {Button} from "@/components/atoms/button.tsx";
import useLoginForm from "@/features/auth/hooks/useLoginForm.ts";

export function LoginForm({
                              className,
                              ...props
                          }: React.ComponentProps<"div">) {

    const {form, isLoading, onsubmit} = useLoginForm()

    return (
        <div className={cn("flex flex-col gap-6", className)} {...props}>
            <Card>
                <CardHeader>
                    <CardTitle className="text-center text-xl">Login to your account</CardTitle>
                    <CardDescription>
                        Enter your email below to login to your account
                    </CardDescription>
                </CardHeader>
                <CardContent>
                    <form onSubmit={form.handleSubmit(onsubmit)}>
                        <FieldGroup>
                            <Field>
                                <FieldLabel htmlFor="email">Email</FieldLabel>
                                <Input
                                    {...form.register("email")}
                                    id="email"
                                    type="email"
                                    placeholder="m@example.com"
                                    required
                                />
                            </Field>
                            <Field>
                                <div className="flex items-center">
                                    <FieldLabel htmlFor="password">Password</FieldLabel>
                                    <a
                                        href="#"
                                        className="ml-auto inline-block text-sm underline-offset-4 hover:underline"
                                    >
                                        Forgot your password?
                                    </a>
                                </div>
                                <Input {...form.register("password")} id="password" type="password" required/>
                            </Field>
                            <Field>
                                <Button type="submit">
                                    {isLoading ? "Loading..." : "Login"}
                                </Button>
                            </Field>
                        </FieldGroup>
                    </form>
                </CardContent>
            </Card>
        </div>
    )
}
